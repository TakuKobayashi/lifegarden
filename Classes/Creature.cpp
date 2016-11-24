#include "Creature.hpp"

Creature::Creature(std::string fileName)
{
    mFileName = fileName;
    mSprite = cocos2d::Sprite::create(fileName);
    mSprite->setPosition(getRandomPosition());
}

Creature::~Creature()
{
    delete(mSprite);
}

cocos2d::Vec2 Creature::getRandomPosition(){
    auto windowSize = cocos2d::Director::getInstance()->getWinSize();
    auto spriteSize = mSprite->getContentSize();
    return cocos2d::Vec2(rand() % (int)(windowSize.width - spriteSize.width), rand() % (int)(windowSize.height - spriteSize.height));
}

cocos2d::Sprite* Creature::getSprite(){
    return mSprite;
}

std::string Creature::getFilename(){
    return mFileName;
}

void Food::spoil(){
    if(mIsSpoiled == true) return;
    size_t f = mFileName.find("1");
    mFileName.replace(f, std::string("1").length(), "2");
    mSprite->setTexture(mFileName);
    mIsSpoiled = true;
}

void Food::executeRandomMove(){
    //mSprite->runAction(cocos2d::MoveTo::create(10, getRandomPosition()));
    
    auto action = cocos2d::MoveTo::create(10, getRandomPosition());
    // ラムダ式でコールバックを設定する
    auto callback = cocos2d::CallFunc::create([this](){
        executeRandomMove();
    });
    // アクションとコールバックをシーケンスに設定する
    auto seq = cocos2d::Sequence::create(action, callback, NULL);
    mSprite->runAction(seq);
}
