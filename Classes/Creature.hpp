#ifndef Creature_hpp
#define Creature_hpp

#include "cocos2d.h"

class Creature{
public:
    Creature(std::string fileName); //コンストラクタ
    bool mIsSpoiled = false;
    std::string mFileName;
    virtual ~Creature();
    cocos2d::Sprite* getSprite();
    void executeRandomMove();
    void spoil();
    std::string getFilename();
private:
    cocos2d::Sprite* mSprite;
    cocos2d::Vec2 getRandomPosition();
};

#endif /* Creature_hpp */
