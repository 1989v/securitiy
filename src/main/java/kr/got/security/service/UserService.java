package kr.got.security.service;

import kr.got.security.domain.dto.AccountDto;
import kr.got.security.domain.entity.Account;

import java.util.List;

public interface UserService {
    void createUser(Account account);

    void modifyUser(AccountDto accountDto);

    List<Account> getUsers();

    AccountDto getUser(Long id);

    void deleteUser(Long idx);

    void order();
}
