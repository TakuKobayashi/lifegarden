#include "HelloWorldScene.h"
#include "SimpleAudioEngine.h"

USING_NS_CC;

Scene* HelloWorld::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::create();
    
    // 'layer' is an autorelease object
    auto layer = HelloWorld::create();

    // add layer as a child to scene
    scene->addChild(layer);

    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool HelloWorld::init()
{
    //////////////////////////////
    // 1. super init first
    if ( !Layer::init() )
    {
        return false;
    }
    
    auto visibleSize = Director::getInstance()->getVisibleSize();
    Vec2 origin = Director::getInstance()->getVisibleOrigin();

    /////////////////////////////
    // 2. add a menu item with "X" image, which is clicked to quit the program
    //    you may modify it.

    // add a "close" icon to exit the progress. it's an autorelease object
    auto closeItem = MenuItemImage::create(
                                           "CloseNormal.png",
                                           "CloseSelected.png",
                                           CC_CALLBACK_1(HelloWorld::menuCloseCallback, this));
    
    closeItem->setPosition(Vec2(origin.x + visibleSize.width - closeItem->getContentSize().width/2 ,
                                origin.y + closeItem->getContentSize().height/2));

    // create menu, it's an autorelease object
    auto menu = Menu::create(closeItem, NULL);
    menu->setPosition(Vec2::ZERO);
    this->addChild(menu, 1);

    /////////////////////////////
    // 3. add your codes below...

    // add a label shows "Hello World"
    // create and initialize a label
    
    auto label = Label::createWithTTF("Hello World", "fonts/Marker Felt.ttf", 24);
    
    // position the label on the center of the screen
    label->setPosition(Vec2(origin.x + visibleSize.width/2,
                            origin.y + visibleSize.height - label->getContentSize().height));

    // add the label as a child to this layer
    this->addChild(label, 1);

    auto bg = Sprite::create("res/bg.png");
    bg->setScale(4.5, 4.5);
    // position the sprite on the center of the screen
    bg->setPosition(Vec2(visibleSize.width/2 + origin.x, visibleSize.height/2 + origin.y));
    this->addChild(bg, 0);

    // add "HelloWorld" splash screen"
    auto farm = Sprite::create("res/farm/farmB.png");
    farm->setScale(6.0, 6.0);

    // position the sprite on the center of the screen
    farm->setPosition(Vec2(visibleSize.width/2 + origin.x, visibleSize.height/2 + origin.y));

    // add the sprite as a child to this layer
    this->addChild(farm, 1);

    /*
    // cocos2d::extension::scrollViewを作る
    m_scrollView = cocos2d::extension::ScrollView::create(this->getContentSize());
     
        // scrollViewに、ノードをセット
        m_scrollView->setContainer(m_scene);
     
        // ズーム用
    　　　// デフォルトではズームの最大値と最小値が1.0倍になっているため、
    　　　// ズームさせたい場合はそれぞれ最大値と最小値をセットしておく
        m_scrollView->setMinScale(1.0f);
        m_scrollView->setMaxScale(3.0f);
     
        // コンテンツサイズをセット
        m_scrollView->setContentSize(Size(m_scene->getContentSize().width, m_scene->getContentSize().height));
     
        // スクロールの向きをセット
        m_scrollView->setDirection(cocos2d::extension::ScrollView::Direction::BOTH);
     
        // スクロールイベント取得するためのDelegate
        m_scrollView->setDelegate(this);
     
        this->addChild(m_scrollView);
    */
    
    // シングルタッチイベントリスナーを作成する。
    auto listener = EventListenerTouchOneByOne::create();
    // スワロータッチモードにするとonTouchBeganメソッドはタッチイベントは他では使われない。
    listener->setSwallowTouches(true);
    
    //タッチ開始
    listener->onTouchBegan = [](Touch* touch, Event* event){
        //target : ターゲットのスプライト
        auto target = (Sprite*)event->getCurrentTarget();
        
        //targetBox : タッチされたスプライトの領域
        Rect targetBox = target->getBoundingBox();
        
        //touchPoint : タッチされた場所
        Point touchPoint = Vec2(touch->getLocationInView().x, touch->getLocationInView().y);
        
        //touchPointがtargetBoxの中に含まれているか判定
        if (targetBox.containsPoint(touchPoint))
        {
            log("タグは%dです", target->getTag());
            return true;
        }
        
        // タッチ位置のログ出力
        log("Touch at (%f, %f)", touch->getLocation().x, touch->getLocation().y);
        
        return true;
    };
    
    //タッチ中
    listener->onTouchMoved = [](Touch* touch, Event* event){
        
        /* ラムダキャプチャ
         [=] : 全てのオブジェクトのコピーがラムダ式に渡されます。
         [&] : 全てのオブジェクトの参照がラムダ式に渡されます。
         [obj] :objのコピーがラムダ式に渡されます。
         [&obj]:objの参照がラムダ式に渡されます。
         */
        
        // タッチ中に動いた時の処理
        auto target = (Sprite*)event->getCurrentTarget();
        
        // タッチ位置が動いた時
        // 前回とのタッチ位置との差をベクトルで取得する
        Vec2 delta = touch->getDelta();
        
        // 現在のかわずたんの座標を取得する
        Vec2 position = target->getPosition();
        
        // 現在座標　+ 移動量を新たな座標にする
        Vec2 newPosition = position + delta;
        
        target->setPosition(newPosition);
    };
    
    //タッチ終了
    listener->onTouchEnded = [](Touch* touch, Event* event){
        log("TouchEnded");
    };
    
    // イベントリスナーをスプライトに追加する。
    this->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, this);
    
    return true;
}


void HelloWorld::menuCloseCallback(Ref* pSender)
{
    //Close the cocos2d-x game scene and quit the application
    Director::getInstance()->end();

    #if (CC_TARGET_PLATFORM == CC_PLATFORM_IOS)
    exit(0);
#endif
    
    /*To navigate back to native iOS screen(if present) without quitting the application  ,do not use Director::getInstance()->end() and exit(0) as given above,instead trigger a custom event created in RootViewController.mm as below*/
    
    //EventCustom customEndEvent("game_scene_close_event");
    //_eventDispatcher->dispatchEvent(&customEndEvent);
    
    
}
