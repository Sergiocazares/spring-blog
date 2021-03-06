package com.example.demo.controllers;

import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.repositories.PostRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.EmailService;
import com.example.demo.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PostController {

    private final PostRepository postsDao;

    private final UserRepository userDao;

    private final EmailService emailService;

    private final UserService userService;

    public PostController(PostRepository postsDao, UserRepository userDao, EmailService emailService, UserService userService){
        this.postsDao = postsDao;
        this.userDao = userDao;
        this.emailService = emailService;
        this.userService = userService;
    }

    @GetMapping("/posts")
    public String postsIndex(Model model) {

        model.addAttribute("posts", postsDao.findAll());
        return "posts/index";
    }

    @GetMapping("/posts/{id}")
    public String postView(Model model, @PathVariable long id) {

        Post post = postsDao.getOne(id);
        model.addAttribute("post", post);
        return "posts/show";
    }

    @GetMapping("/posts/{id}/edit")
    public String viewEditPostForm(@PathVariable Long id, Model model) {
        model.addAttribute("post", postsDao.getOne(id));
        return "posts/edit";
    }

    @PostMapping("/posts/{id}/edit")
    public String updatePost(@PathVariable Long id, @ModelAttribute Post post) {

        User user = userDao.findAll().get(0);
        post.setUser(user);

        postsDao.save(post);
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable long id){
        System.out.println("Deleting post...");
        postsDao.deleteById(id);
        return "redirect:/posts";
    }

    @GetMapping("/posts/create")
    public String postForm(Model model){

        model.addAttribute("post", new Post());

        return "posts/create";
    }

    @PostMapping("/posts/create")
    public String createPost(@ModelAttribute Post post) {

        User user = userService.loggedInUser();
        post.setUser(user);

        Post savedPost = postsDao.save(post);
        String subject = "New Ad Created: " + savedPost.getTitle();
        String body = "Dear " + savedPost.getUser().getUsername() + ", Thank you for creating an Ad. Your ad is: " + savedPost.getId();

        emailService.prepareAndSend(savedPost, subject, body);
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/posts/profile")
    public String profileView(Model model) {

        User user = userService.loggedInUser();
        model.addAttribute("user", user);
        model.addAttribute("posts", postsDao.findAllByUserId(user.getId()));
//        Post post = postsDao.getOne(id);
//        model.addAttribute("post", post);
        return "posts/profile";
    }
}
