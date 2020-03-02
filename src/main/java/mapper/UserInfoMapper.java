package mapper;

import entity.UserInfo;

import java.util.List;

public interface UserInfoMapper {
    void updateUserInfo(UserInfo userInfo);
    void insertUserInfo(UserInfo userInfo);
    UserInfo getUserInfo(int id);
    void insertBatch(List<UserInfo> userInfos);

}
