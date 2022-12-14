package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "settings", method = RequestMethod.GET)
    public String getSettingsPage() {
        return "/site/settings";
    }


    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "No images selected.");
            return "/site/settings";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "Incorrect image format.");
            return "/site/settings";
        }
        try {
            Files.createDirectories(Paths.get(uploadPath));
            // generating random file name
            fileName = CommunityUtil.generateUUID() + suffix;
            // determining file storage path
            File dest = new File(uploadPath + "/" + fileName);
            // saving file
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("File upload failure: " + e.getMessage());
            throw new RuntimeException("File upload failed. Server error.", e);
        }

        // updating current user header's path (web path)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // server storage path
        fileName = uploadPath + "/" + fileName;
        // file suffix
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // responding to request
        response.setContentType("image/" + suffix);
        try (OutputStream os = response.getOutputStream();
             FileInputStream fis = new FileInputStream(fileName);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("Reading profile picture failed: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public String changePassword(HttpServletRequest request, String oldPassword, String newPassword, String confirmPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword, confirmPassword);
        if (map == null || map.isEmpty()) {
            Cookie[] cookies = request.getCookies();
            String ticket = "";
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("ticket")) {
                        ticket = cookie.getName();
                    }
                }
            }
            userService.logout(ticket);
            return "redirect:/login";
        } else {
            model.addAttribute("oldPasswordMessage", map.get("oldPasswordMessage"));
            model.addAttribute("newPasswordMessage", map.get("newPasswordMessage"));
            model.addAttribute("confirmPasswordMessage", map.get("confirmPasswordMessage"));
            return "/site/settings";
        }
    }

    // profile
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }

        // user
        model.addAttribute("user", user);
        // like count
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // following count
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // followee count
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // has followed
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }

        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }


}
