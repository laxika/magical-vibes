package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.g.GoblinWardriver;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.b.BlackSunsZenith;
import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.cards.l.LoxodonPartisan;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.DrossRipper;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        PhyrexianCrusader.class, GrizzlyBears.class, UnholyStrength.class, GoblinWardriver.class,
        GoForTheThroat.class, WhiteKnight.class, BlackKnight.class, BlackSunsZenith.class,
        OgreResister.class, LoxodonPartisan.class, LightningBolt.class, SengirVampire.class,
        SwordsToPlowshares.class, Slagstorm.class, HolyStrength.class, CrawWurm.class,
        DrossRipper.class
})
class PhyrexianCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Phyrexian Crusader puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PhyrexianCrusader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Crusader");
    }

    @Test
    @DisplayName("Resolving puts Phyrexian Crusader on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PhyrexianCrusader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Crusader");
    }

    @Test
    @DisplayName("Red creature cannot block Phyrexian Crusader")
    void redCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GoblinWardriver());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("White creature cannot block Phyrexian Crusader")
    void whiteCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Black creature can block Phyrexian Crusader")
    void blackCreatureCanBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BlackKnight());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Phyrexian Crusader takes no combat damage from red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new PhyrexianCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Phyrexian Crusader");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Phyrexian Crusader takes no combat damage from white creature")
    void takesNoDamageFromWhite() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new LoxodonPartisan());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new PhyrexianCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Phyrexian Crusader");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Phyrexian Crusader takes normal combat damage from black creature")
    void takesNormalDamageFromBlack() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertNotOnBattlefield(player1, "Phyrexian Crusader");
        harness.assertInGraveyard(player1, "Phyrexian Crusader");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player2, new PhyrexianCrusader());
        crusader.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be targeted by white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player2, new PhyrexianCrusader());
        crusader.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by black instant")
    void canBeTargetedByBlackInstant() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        crusader.setSummoningSick(false);

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, crusader.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Go for the Throat");
    }

    @Test
    @DisplayName("Cannot be enchanted by white aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player2, new PhyrexianCrusader());
        crusader.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be enchanted by black aura (Unholy Strength)")
    void canBeEnchantedByBlackAura() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        crusader.setSummoningSick(false);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, crusader.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unholy Strength");
    }

    @Test
    @DisplayName("Unblocked Phyrexian Crusader deals poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Phyrexian Crusader deals -1/-1 counters to blocker instead of regular damage")
    void dealsMinusCountersToBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        blocker.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInGraveyard(player1, "Phyrexian Crusader");

        Permanent survivingBlocker = findPermanent(player2, "Craw Wurm");
        assertThat(survivingBlocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("First strike with infect kills 2/2 blocker with -1/-1 counters before it deals damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Phyrexian Crusader");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Protection prevents red damage from a spell that does not target")
    void preventsUntargetedRedDamage() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Slagstorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Crusader");
        assertThat(crusader.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("First-strike infect reduces the blocker's regular combat damage")
    void infectReducesReturnDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DrossRipper());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Phyrexian Crusader");
        harness.assertOnBattlefield(player2, "Dross Ripper");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A black Aura attaches successfully and increases infect damage")
    void blackAuraIncreasesPoisonDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Unholy Strength").getAttachedTo()).isEqualTo(attacker.getId());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Protection does not stop counters from a spell that does not deal damage")
    void diesToUntargetedMinusCounters() {
        harness.addToBattlefield(player1, new PhyrexianCrusader());
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Crusader");
        harness.assertInGraveyard(player1, "Phyrexian Crusader");
    }
}
