// CS 2510, Assignment 6
//Tunwa Tongtawee
//Alex Kouyoumjian

import tester.*;

import java.util.Random;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.function.Predicate;



// Abstract class for a standard fish
abstract class AFishInfo {
  int radius;
  int posnX;
  int posnY;
  Color color;

  AFishInfo(int radius, int posnX, int posnY, Color color) {
    this.radius = radius;
    this.posnX = posnX;
    this.posnY = posnY;
    this.color = color;
    
  }

  //draw this BFish as CircleImage with its size and color. for now fish = circle
  WorldScene draw(WorldScene ws) {
  
    return ws.placeImageXY(new CircleImage(this.radius, "solid", this.color),
        this.posnX, this.posnY);
  }
}



// class for the user controlled fish
class UFish extends AFishInfo {

  UFish(int radius, int posnX, int posnY, Color color) {
    super(radius, posnX, posnY, color);
  }

  // is this fish within range of other fish
  public boolean withInRange(BFish other) {
    return Math.abs(this.posnX - other.posnX) <= Math.max(this.radius, other.radius)
        && Math.abs(this.posnY - other.posnY) <= Math.max(this.radius, other.radius);
  }

  // is this fish bigger than the other fish
  // if same radius as other, returns false as the fish is not bigger
  // if it is bigger, the other fish is eaten, resulting in two things:
  // mutates other's alive boolean to false to show it is not alive
  // mutates this fish's radius, growing more/less based on size of other fish.
  public boolean isBigger(BFish other) {
    if (this.radius > other.radius) {
      other.alive = false;
      this.radius = this.radius + (Math.round(other.radius / 10));
      return true;
    }
    else {
      return false;
    }
  }
}

//class for the background fish
class BFish extends AFishInfo {
  boolean alive;
  Random rand;
  int velocity;

  BFish(int radius, int posnX, int posnY, Color color, boolean alive) {
    super(radius, posnX, posnY, color);
    this.alive = alive;
    this.rand = new Random();
    this.velocity = this.rand.nextInt(40) - 20;
  }

  BFish(int radius, int posnX, int posnY, Color color, boolean alive, Random random) {
    super(radius, posnX, posnY, color);
    this.alive = alive;
    this.rand = random;
    this.velocity = this.rand.nextInt(40) - 20;
  }

  // moves the background fish randomly by mutating its posnX and posnY.
  // returns the newly mutated BFish
  BFish moveRandom() {
    this.posnX = this.posnX + this.velocity;
    if (this.posnX > 1000) {
      this.posnX = 5;
      return this;
    }
    else {
      if (this.posnX < 0) {
        this.posnX = 995;
        return this;
      }
      else {
        return this;
      }
    }
  }
}


// class for list of T
interface IList<T> {

  //combines the items in this list using the given function
  <U> U foldr(BiFunction<T, U, U> fun, U base);

  //maps a func onto each member of list, returns a list of the results
  <U> IList<U> map(Function<T, U> fun);

  //filters this list by the given predicate
  IList<T> filter(Predicate<T> pred);

  // does the predicate return false when tested on any item in the list
  public boolean ormap(Predicate<T> pred);

  //maps a function onto each member of the list, producing a list of the results returning true
  public boolean andmap(Predicate<T> pred);
}

// class for empty list of T
class EmptyList<T> implements IList<T> {

  //combines the items in this list using the given function
  public <U> U foldr(BiFunction<T, U, U> fun, U base) {
    return base;
  }

  //maps a func onto each member of list, returns a list of the results
  public <U> IList<U> map(Function<T, U> fun) {
    return new EmptyList<U>();
  }

  //filter this list by the given predicate
  public IList<T> filter(Predicate<T> pred) {
    return this;
  }

  // does the predicate return false when tested on any item in the list
  public boolean ormap(Predicate<T> pred) {
    return false;
  }

  // does the predicate return false when tested on all item in the list
  public boolean andmap(Predicate<T> pred) {
    return true;
  }

}

// list of background fish
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }


  //combines the items in this list using the given function
  public <U> U foldr(BiFunction<T, U, U> fun, U base) {
    return fun.apply(this.first, this.rest.foldr(fun, base));
  }

  //maps a function onto each member of the list, producing a list of the results
  public <U> IList<U> map(Function<T, U> fun) {
    return new ConsList<U>(fun.apply(this.first), this.rest.map(fun));
  }

  //maps a function onto each member of the list, producing a list of the results
  public boolean ormap(Predicate<T> pred) {
    return pred.test(this.first) || this.rest.ormap(pred);
  }

  //maps a function onto each member of the list, producing a list of the results
  public boolean andmap(Predicate<T> pred) {
    return pred.test(this.first) && this.rest.andmap(pred);
  }

  //filter this list by the given predicate
  public IList<T> filter(Predicate<T> pred) {
    if (pred.test(this.first)) {
      return new ConsList<T>(this.first, this.rest.filter(pred));
    }
    else {
      return this.rest.filter(pred);
    }
  }

}

// class to place fish onto a ws. BiFunction<T, U, V>
// T = first arg. U = Second arg. V = result of function
class DrawAll implements BiFunction<BFish, WorldScene, WorldScene> {
  public WorldScene apply(BFish fish, WorldScene ws) {
    return fish.draw(ws); } }
// class to move fish. Function<T = Type of Input, U = Result>
class MoveAll implements Function<BFish, BFish> {
  public BFish apply(BFish fish) {
    return fish.moveRandom();
  }
}


// class to filter out dead fish
class DeadFilter implements Predicate<BFish> {

  // eliminates the dead fish
  public boolean test(BFish f) {
    return f.alive;
  }
}

// is the given fish bigger than all fish in the list
class IsBiggest implements Predicate<BFish> {
  UFish ufish;

  IsBiggest(UFish ufish) {
    this.ufish = ufish;
  }

  // is the given fish bigger than other fish
  public boolean test(BFish f) {
    return this.ufish.radius > f.radius;
  }
}

// has the ufish collided with given fish
class HasCollided implements Predicate<BFish> {
  UFish ufish;

  HasCollided(UFish ufish) {
    this.ufish = ufish;
  }

  // his the ufish collided with any fish in the list
  public boolean test(BFish fish) {
    if (this.ufish.withInRange(fish)) {
      return this.ufish.isBigger(fish);
    }
    else {
      return false;
    }
  }
}

//has the ufish been eaten by given fish
class HasBeenEaten implements Predicate<BFish> {
  UFish ufish;

  HasBeenEaten(UFish ufish) {
    this.ufish = ufish;
  }

  // his the ufish been eaten by any fish in the list
  public boolean test(BFish fish) {
    if (this.ufish.withInRange(fish)) {
      return !this.ufish.isBigger(fish);
    }
    else {
      return false;
    }
  }
}



// World class for the Background Fish
class FishWorld extends World {
  IList<BFish> loF; // list of background fish
  UFish ufish;


  FishWorld(IList<BFish> loF, UFish ufish) {
    this.loF = loF;
    this.ufish = ufish;
  }

  //draws the background fish onto the background with the user fish
  public WorldScene makeScene() {
    return this.ufish.draw(this.loF.foldr(new DrawAll(), new WorldScene(1000, 1000)));
  }

  // moves the UFish by pressing the following keys
  // w = up, s = down, a = left, d = right by key press
  public World onKeyEvent(String keyName) {
    if (keyName.equals("w")) {
      this.ufish.posnY = this.ufish.posnY - 10;
      return this;
    } else if (keyName.equals("s")) {
      this.ufish.posnY = this.ufish.posnY + 10;
      return this;
    } else if (keyName.equals("a")) {
      if (ufish.posnX > 1000) {
        ufish.posnX = 5;
        return this;
      }
      else {
        if (ufish.posnX < 0) {
          ufish.posnX = 995;
          return this;
        } else {
          this.ufish.posnX = this.ufish.posnX - 10;
        }
        return this;
      }
    } else if (keyName.equals("d")) {
      if (ufish.posnX > 1000) {
        ufish.posnX = 5;
        return this;
      }
      else {
        if (ufish.posnX < 0) {
          ufish.posnX = 995;
          return this;
        } else {
          this.ufish.posnX = this.ufish.posnX + 10;
        }
        return this;
      }
    } else {
      return this;
    }
  }

  // checks the list to see if any fish are dead, and removes them from list
  public World onTick() {
    this.loF = this.loF.filter(new DeadFilter());
    this.loF = this.loF.map(new MoveAll());

    if (this.loF.ormap(new HasCollided(this.ufish))) {
      return this.endOfWorld("Game Over! You Lose!");
    }
    else if (this.loF.andmap(new IsBiggest(this.ufish))) {
      return this.endOfWorld("YOU WIN!");
    }
    else {
      return this;
    }
  }

  public WorldEnd worldEnds() {
    if (this.loF.ormap(new HasBeenEaten(this.ufish))) {
      return new WorldEnd(true, new WorldScene(1000, 1000).placeImageXY(
          new TextImage("Game Over! You lose!", 50, Color.red), 500, 500));
    }
    else if (this.loF.andmap(new IsBiggest(this.ufish))) {
      return new WorldEnd(true, new WorldScene(1000, 1000).placeImageXY(
          new TextImage("You Win!", 50, Color.red), 500, 500));
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}


// Examples and Tests
class ExampleFish {

  //random seed
  Random random1 = new Random(1);

  IList<BFish> mt = new EmptyList<BFish>();

  WorldScene ws0 = new WorldScene(1000, 1000);
  WorldScene ws1 = new WorldScene(1000, 1000).placeImageXY(
      new CircleImage(10, "solid", Color.black), 500, 0);
  WorldScene wsBFish10 = new WorldScene(1000, 1000).placeImageXY(
      new CircleImage(30, "solid", Color.green), 1000, 200);
  WorldScene ft1 = new WorldScene(1000, 1000).placeImageXY(
      new CircleImage(10,"solid", Color.red),100,100);
  WorldScene ft2 = new WorldScene(1000, 1000).placeImageXY(
      new CircleImage(10,"solid", Color.red),100,100).placeImageXY((
          new CircleImage(20,"solid", Color.red)),200,600);
  World fw1 = new FishWorld(this.fishList10, this.ufish);

  boolean testBigBang(Tester t) {
    FishWorld world = new FishWorld(this.fishList10, this.ufish);
    int worldWidth = 1000;
    int worldHeight = 1000;
    double tickRate = .1;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }

  // examples of background fish. change all to make start at 0 or 1000.
  BFish bfish1 = new BFish(10, 100, 100, Color.red, true, random1);
  BFish bfish2 = new BFish(20, 200, 600, Color.red, true, random1);
  BFish bfish3 = new BFish(75, 300, 300, Color.orange, true, random1);
  BFish bfish4 = new BFish(25, 400, 10, Color.orange, true, random1);
  BFish bfish5 = new BFish(60, 500, 5, Color.pink, true, random1);
  BFish bfish6 = new BFish(50, 545, 15, Color.pink, true, random1);
  BFish bfish7 = new BFish(20, 700, 0, Color.magenta, true, random1);
  BFish bfish8 = new BFish(60, 800, 450, Color.magenta, true, random1);
  BFish bfish9 = new BFish(40, 900, 100, Color.yellow, true, random1);
  BFish bfish10 = new BFish(30, 1000, 200, Color.green, true, random1);

  // examples of background fish move randomed
  BFish bfish1mr = new BFish(10, 105,
      100, Color.red, true, random1);
  BFish bfish2mr = new BFish(20, 200,
      600, Color.red, true, random1);
  BFish bfish3mr = new BFish(75, 300,
      300, Color.orange, true, random1);

  // example of UFish
  UFish ufish = new UFish(20, 100, 500, Color.black);
  UFish ufish1 = new UFish(10, 500, 0, Color.black);

  UFish ufishBig = new UFish(100, 500, 0, Color.black);

  // fish made for tests
  UFish ufishGrown1 = new UFish(11, 500, 0, Color.black);
  UFish ufishGrown2 = new UFish(16, 500, 0, Color.black);
  UFish ufishMoveW = new UFish(20, 100, 510, Color.black);
  UFish ufishMoveS = new UFish(20, 100, 490, Color.black);
  UFish ufishMoveA = new UFish(20, 90, 500, Color.black);
  UFish ufishMoveD = new UFish(20, 110, 500, Color.black);

  BFish bfishUFish = new BFish(10, 500, 0, Color.black, true, random1);
  BFish bfishSmall = new BFish(1, 500, 0, Color.white, true, random1);
  BFish bfishSmall2 = new BFish(9, 500, 0, Color.white, true);
  BFish bfishclose1 = new BFish(10, 500, 5, Color.pink, true, random1);
  BFish bfishclose2 = new BFish(10, 505, 0, Color.pink, true, random1);
  BFish bfishNoSize = new BFish(0, 500, 0, Color.white, true, random1);
  BFish bfishOnUFish1 = new BFish(1, 500, 0, Color.black, true, random1);
  BFish bfishDead = new BFish(10, 100, 100, Color.red, false, random1);


  // list of example fish
  IList<BFish> fishList1 = new ConsList<BFish>(this.bfish1, this.mt);
  IList<BFish> fishList2 = new ConsList<BFish>(this.bfish2, this.fishList1);
  IList<BFish> fishList3 = new ConsList<BFish>(this.bfish3, this.fishList2);
  IList<BFish> fishList4 = new ConsList<BFish>(this.bfish4, this.fishList3);
  IList<BFish> fishList5 = new ConsList<BFish>(this.bfish5, this.fishList4);
  IList<BFish> fishList6 = new ConsList<BFish>(this.bfish6, this.fishList5);
  IList<BFish> fishList7 = new ConsList<BFish>(this.bfish7, this.fishList6);
  IList<BFish> fishList8 = new ConsList<BFish>(this.bfish8, this.fishList7);
  IList<BFish> fishList9 = new ConsList<BFish>(this.bfish9, this.fishList8);
  IList<BFish> fishList10 = new ConsList<BFish>(this.bfish10, this.fishList9);

  IList<BFish> fishList1Move = new ConsList<BFish>(this.bfish1mr, this.mt);
  IList<BFish> fishList2Move = new ConsList<BFish>(this.bfish2mr, this.fishList1Move);

  IList<BFish> fishListCollidedShort = new ConsList<BFish>(this.bfishOnUFish1, this.mt);
  IList<BFish> fishListCollidedLong = new ConsList<BFish>(this.bfishOnUFish1, this.fishList10);

  IList<BFish> fishlistSmall1 = new ConsList<BFish>(this.bfishSmall, this.mt);
  IList<BFish> fishlistSmall2 = new ConsList<BFish>(this.bfishSmall2, this.fishlistSmall1);
  IList<BFish> fishList10PlusDead = new ConsList<BFish>(this.bfishDead, this.fishList10);





  // initializes the data
  void initData() {
    this.ufish1 = new UFish(10, 500, 0, Color.black);
    this.ufishBig = new UFish(100, 500, 0, Color.black);

    this.bfish1 = new BFish(10, 100, 100, Color.red, true);
    this.bfish2 = new BFish(20, 200, 600, Color.red, true);
    this.bfish3 = new BFish(75, 300, 300, Color.orange, true);
    this.bfish4 = new BFish(25, 400, 10, Color.orange, true);
    this.bfish5 = new BFish(60, 500, 5, Color.pink, true);

    this.bfishSmall = new BFish(1, 500, 0, Color.white, true);



    this.fishList1 = new ConsList<BFish>(this.bfish1, this.mt);
    this.fishList2 = new ConsList<BFish>(this.bfish2, this.fishList1);
    this.fishList3 = new ConsList<BFish>(this.bfish3, this.fishList2);
    this.fishList4 = new ConsList<BFish>(this.bfish4, this.fishList3);
    this.fishList5 = new ConsList<BFish>(this.bfish5, this.fishList4);
  }


  // tests for draw method in AFishInfo abstract class
  boolean testDraw(Tester t) {
    return t.checkExpect(this.ufish1.draw(this.ws0), this.ws1)
        && t.checkExpect(this.bfish10.draw(this.ws0), this.wsBFish10);
  }

  // tests for moveRandom method
  boolean testMoveRandom(Tester t) {
    return t.checkExpect(this.bfish1.moveRandom(), bfish1mr)
        && t.checkExpect(this.bfish2.moveRandom(), bfish2mr)
        && t.checkExpect(this.bfish3.moveRandom(), bfish3mr);
  }

  // tests for withInRange method
  boolean testWithInRange(Tester t) {
    return t.checkExpect(this.ufish1.withInRange(this.bfish1), false)
        // neither x or y are within range
        && t.checkExpect(this.ufish1.withInRange(this.bfishclose1), true)
        // tests for on same x, only 5 lower
        && t.checkExpect(this.ufish1.withInRange(this.bfishclose2), true)
        // test for only 5 to right, 5 higher
        && t.checkExpect(this.ufish1.withInRange(this.bfish6), true)
        // test for radius makes it w in range
        && t.checkExpect(this.ufish1.withInRange(this.bfishUFish), true);
    // both x and y are within range
  }

  // tests for isBigger method
  boolean testIsBigger(Tester t) {
    return t.checkExpect(this.ufish1.isBigger(this.bfish1), false)
        //fish = same size, so isBigger -> false
        && t.checkExpect(this.ufish1.isBigger(this.bfish8), false)
        && t.checkExpect(this.ufish1.isBigger(this.bfishSmall), true);
  }

  // tests for growFish, testing the mutation of the BFish's boolean: alive
  // if smaller, boolean should be mutated to false.
  void testIsBiggerBooleanMutation(Tester t) {
    this.initData();
    t.checkExpect(this.bfish2.alive, true);
    t.checkExpect(this.bfishSmall.alive, true);
    this.ufish1.isBigger(this.bfish2);
    this.ufish1.isBigger(this.bfishSmall);
    t.checkExpect(this.bfish2.alive, true);
    t.checkExpect(this.bfishSmall.alive, false);
  }

  // Tests isBigger method for mutation of this fish's radius since it grows
  // by a fraction of other fish is bigger than.
  void testIsBiggerGrowMutation(Tester t) {
    this.initData();
    t.checkExpect(this.ufish1.radius, 10);
    t.checkExpect(this.bfish2.radius, 20);
    this.ufish1.isBigger(this.bfish2);
    t.checkExpect(this.ufish1.radius, 10); //test for case if UFish is not bigger

    this.initData();
    t.checkExpect(this.ufishBig.radius, 100);
    t.checkExpect(this.bfish2.radius, 20);
    this.ufishBig.isBigger(this.bfish2);
    t.checkExpect(this.ufishBig.radius, 102); // test for case if UFish is bigger
  }

  // tests for HasCollided, HasBeenEaten through ormap
  boolean testOrMap(Tester t) {
    return t.checkExpect(this.mt.ormap(new HasCollided(this.ufish1)),
        false)
        && t.checkExpect(this.fishList3.ormap(new HasCollided(this.ufish1)),
            false)
        && t.checkExpect(this.fishListCollidedShort.ormap(new HasCollided(this.ufish1)),
            true)
        && t.checkExpect(this.fishListCollidedLong.ormap(new HasCollided(this.ufish1)),
            true)
        && t.checkExpect(this.fishListCollidedLong.ormap(new HasBeenEaten(this.ufish1)),
            true)
        && t.checkExpect(this.fishList3.ormap(new HasBeenEaten(this.ufish1)),
            false);
  }

  // tests for IsBiggest through andmap
  boolean testAndMap(Tester t) {
    return t.checkExpect(this.mt.andmap(new IsBiggest(this.ufish1)), true)
        && t.checkExpect(this.fishlistSmall2.andmap(new IsBiggest(this.ufish1)), true)
        && t.checkExpect(this.fishList10.andmap(new IsBiggest(this.ufish1)), false);
  }


  // tests for DeadFilter through filter
  boolean testDeadFilter(Tester t) {
    return t.checkExpect(this.mt.filter(new DeadFilter()), this.mt)
        && t.checkExpect(this.fishList10.filter(new DeadFilter()), this.fishList10)
        && t.checkExpect(this.fishList10PlusDead.filter(new DeadFilter()), this.fishList10);
  }

  // tests for KeyEvent
  boolean testKeyEvent(Tester t) {
    return t.checkExpect(fw1.onKeyEvent("w"),ufishMoveW)
        && t.checkExpect(fw1.onKeyEvent("s"),ufishMoveS)
        && t.checkExpect(fw1.onKeyEvent("a"),ufishMoveA)
        && t.checkExpect(fw1.onKeyEvent("d"),ufishMoveD);
  }


  // tests for MoveAll through map
  boolean testMoveAll(Tester t) {
    return t.checkExpect(this.mt.map(new MoveAll()), mt)
        && t.checkExpect(this.fishList2.map(new MoveAll()), fishList2Move);
  }
}


