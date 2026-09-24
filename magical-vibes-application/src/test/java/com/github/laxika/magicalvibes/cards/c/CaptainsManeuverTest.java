package com.github.laxika.magicalvibes.cards.c;
import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.b.BattlefieldForge;
import com.github.laxika.magicalvibes.cards.r.RazorfinHunter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
@CardUsed({CaptainsManeuver.class,AngelfireCrusader.class,BattlefieldForge.class,ChandraNalaar.class,RazorfinHunter.class})
class CaptainsManeuverTest extends BaseCardTest {
 @Test void creatureToCreature() { var p=addCreatureReady(player1,new AngelfireCrusader()); var d=addCreatureReady(player2,new AngelfireCrusader()); var h=addCreatureReady(player2,new RazorfinHunter()); var h2=addCreatureReady(player2,new RazorfinHunter()); cast(2,p.getId(),d.getId()); ping(h,p.getId()); ping(h2,p.getId()); assertThat(p.getMarkedDamage()).isZero(); assertThat(d.getMarkedDamage()).isEqualTo(2); }
 @Test void playerToPlayer() { var h=addCreatureReady(player2,new RazorfinHunter()); harness.setLife(player1,20); harness.setLife(player2,20); cast(1,player1.getId(),player2.getId()); ping(h,player1.getId()); assertThat(gd.getLife(player1.getId())).isEqualTo(20); assertThat(gd.getLife(player2.getId())).isEqualTo(19); }
 @Test void creatureAndPlayerTargets() { var p=addCreatureReady(player1,new AngelfireCrusader()); var h=addCreatureReady(player2,new RazorfinHunter()); var h2=addCreatureReady(player2,new RazorfinHunter()); harness.setLife(player2,20); cast(1,p.getId(),player2.getId()); ping(h,p.getId()); ping(h2,p.getId()); assertThat(p.getMarkedDamage()).isEqualTo(1); assertThat(gd.getLife(player2.getId())).isEqualTo(19); var d=addCreatureReady(player2,new AngelfireCrusader()); var h3=addCreatureReady(player2,new RazorfinHunter()); harness.setLife(player1,20); cast(1,player1.getId(),d.getId()); ping(h3,player1.getId()); assertThat(gd.getLife(player1.getId())).isEqualTo(20); assertThat(d.getMarkedDamage()).isEqualTo(1); }
 @Test void planeswalkerTargets() { var pw=walker(player1,6); var d=addCreatureReady(player2,new AngelfireCrusader()); var h=addCreatureReady(player2,new RazorfinHunter()); cast(1,pw.getId(),d.getId()); ping(h,pw.getId()); assertThat(pw.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); assertThat(d.getMarkedDamage()).isEqualTo(1); var p=addCreatureReady(player1,new AngelfireCrusader()); var pw2=walker(player2,6); var h2=addCreatureReady(player2,new RazorfinHunter()); cast(1,p.getId(),pw2.getId()); ping(h2,p.getId()); assertThat(p.getMarkedDamage()).isZero(); assertThat(pw2.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); }
 @Test void rejectsLandAndSharedTarget() { var land=harness.addToBattlefieldAndReturn(player2,new BattlefieldForge()); var c=addCreatureReady(player2,new AngelfireCrusader()); harness.setHand(player1,List.of(new CaptainsManeuver())); mana(1); assertThatThrownBy(()->harness.castInstantForX(player1,0,1,List.of(land.getId(),c.getId()))).isInstanceOf(IllegalStateException.class); harness.setHand(player1,List.of(new CaptainsManeuver())); mana(1); assertThatThrownBy(()->harness.castInstantForX(player1,0,1,List.of(c.getId(),c.getId()))).isInstanceOf(IllegalStateException.class); }
 @Test void zeroXAndEndOfTurn() { var p=addCreatureReady(player1,new AngelfireCrusader()); var d=addCreatureReady(player2,new AngelfireCrusader()); var h=addCreatureReady(player2,new RazorfinHunter()); cast(0,p.getId(),d.getId()); ping(h,p.getId()); assertThat(p.getMarkedDamage()).isEqualTo(1); assertThat(d.getMarkedDamage()).isZero(); var p2=addCreatureReady(player1,new AngelfireCrusader()); var d2=addCreatureReady(player2,new AngelfireCrusader()); var h2=addCreatureReady(player2,new RazorfinHunter()); cast(1,p2.getId(),d2.getId()); harness.forceStep(TurnStep.END_STEP); harness.clearPriorityPassed(); harness.passBothPriorities(); ping(h2,p2.getId()); assertThat(p2.getMarkedDamage()).isEqualTo(1); assertThat(d2.getMarkedDamage()).isZero(); }
 private void cast(int x,UUID p,UUID d) { harness.setHand(player1,List.of(new CaptainsManeuver())); mana(x); harness.castInstantForX(player1,0,x,List.of(p,d)); harness.passBothPriorities(); }
 private void ping(Permanent h,UUID t) { harness.activateAbility(player2,gd.playerBattlefields.get(player2.getId()).indexOf(h),null,t); harness.passBothPriorities(); }
 private void mana(int x) { harness.addMana(player1,ManaColor.RED,1); harness.addMana(player1,ManaColor.WHITE,1); harness.addMana(player1,ManaColor.COLORLESS,x); }
 private Permanent walker(Player p,int loyalty) { var w=new Permanent(new ChandraNalaar()); w.setCounterCount(CounterType.LOYALTY,loyalty); gd.playerBattlefields.get(p.getId()).add(w); return w; }
}
