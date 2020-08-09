package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
  @Autowired MockMvc mockMvc;
  ObjectMapper objectMapper;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  public void shouldRegisterUser() throws Exception {
    User user = new User("idolice", "female", 19, "a@b.com", "18888888888");
    String request = objectMapper.writeValueAsString(user);
    mockMvc
        .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isOk());
    List<UserDto> all = userRepository.findAll();
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getUserName(), "idolice");
    assertEquals(all.get(0).getEmail(), "a@b.com");
  }

  @Test
  public void shouldNotRegisterWhenNameIsInvalid() throws Exception {
    User user = new User("idolice88", "female", 19, "a@b.com", "18888888888");
    String request = objectMapper.writeValueAsString(user);
    mockMvc
        .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotRegisterWhenAgeIsInvalid() throws Exception {
    User user = new User("idolice", "female", 17, "a@b.com", "18888888888");
    String request = objectMapper.writeValueAsString(user);
    mockMvc
        .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotRegisterWhenEmailIsInvalid() throws Exception {
    User user = new User("idolice", "female", 19, "ab.com", "18888888888");
    String request = objectMapper.writeValueAsString(user);
    mockMvc
        .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotRegisterWhenPhoneIsInvalid() throws Exception {
    User user = new User("idolice", "female", 19, "a@b.com", "188888888880");
    String request = objectMapper.writeValueAsString(user);
    mockMvc
        .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotRegisterWhenNameIsNull() throws Exception {
    User user = new User(null, "female", 19, "a@b.com", "18888888888");
    String request = objectMapper.writeValueAsString(user);
    mockMvc
        .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotRegisterWhenPhoneIsNull() throws Exception {
    User user = new User("idolice", "female", 19, "a@b.com", null);
    String request = objectMapper.writeValueAsString(user);
    mockMvc
        .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldDeleteUser() throws Exception {
    UserDto userDto =
        UserDto.builder()
            .voteNum(10)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("idolice")
            .build();
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyWord("keyWord").eventName("eventName").user(save).build();
    rsEventRepository.save(rsEventDto);

    mockMvc.perform(delete("/user/{id}", save.getId())).andExpect(status().isOk());

    assertEquals(userRepository.findAll().size(), 0);
    assertEquals(rsEventRepository.findAll().size(), 0);
  }
}
