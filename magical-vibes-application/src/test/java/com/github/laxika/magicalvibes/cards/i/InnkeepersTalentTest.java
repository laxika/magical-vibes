package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AetherHub;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PawpatchFormation;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InnkeepersTalent.class, BurstOfStrength.class, GrizzlyBears.class, Shock.class,
        IchorRats.class, PawpatchFormation.class, AetherHub.class})
class InnkeepersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a chosen creature at the beginning of combat")
    void putsCounterAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new InnkeepersTalent());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 2, permanents you control with counters have ward")
    void grantsWardToPermanentsWithCounters() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);

        castOpponentShock(bears);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("At level 3, doubles counters put on an opponent's permanent")
    void doublesCountersOnAnyPermanent() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, opponentBears.getId());

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("At level 3, doubles counters put on any player")
    void doublesCountersOnAnyPlayer() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void levelThreeDoublesItsBeginningOfCombatCounter() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerOnOpponentsCombat() {
        harness.addToBattlefield(player1, new InnkeepersTalent());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void levelTwoDoesNotGrantWardToCreatureWithoutCounters() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOpponentShock(bears);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void levelOneDoesNotGrantWardEvenToCreatureWithCounters() {
        harness.addToBattlefield(player1, new InnkeepersTalent());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);

        castOpponentShock(bears);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void opponentCanPayWardCost() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        castOpponentShock(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotGrantWardToOpponentsPermanentsWithCounters() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void classLevelAloneDoesNotGiveTalentWard() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);

        castOpponentEnchantmentRemoval(talent);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Innkeeper's Talent");
    }

    @Test
    void levelTwoGrantsWardToNoncreaturePermanentWithActualCounter() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        talent.setCounterCount(CounterType.CHARGE, 1);

        castOpponentEnchantmentRemoval(talent);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Innkeeper's Talent");
        harness.assertInGraveyard(player2, "Pawpatch Formation");
    }

    @Test
    void levelCountersDoNotUnlockClassAbilities() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        talent.setCounterCount(CounterType.LEVEL, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);

        castOpponentShock(bears);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void anotherLevelThreeTalentCannotSkipSecondTalentsLevelTwo() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(first, 0, 0);
        levelUp(first, 1, 3);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(second, 0, 0);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void levelThreeDoesNotDoubleCountersPutByOpponent() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void levelThreeDoublesEnergyCounters() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);
        harness.setHand(player1, List.of(new AetherHub()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    void cannotGainLevelThreeBeforeLevelTwo() {
        harness.addToBattlefield(player1, new InnkeepersTalent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotGainClassLevelAtInstantSpeed() {
        harness.addToBattlefield(player1, new InnkeepersTalent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOpponentEnchantmentRemoval(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new PawpatchFormation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, target.getId());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void levelUp(Permanent talent, int abilityIndex, int colorlessMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        if (colorlessMana > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        }
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
