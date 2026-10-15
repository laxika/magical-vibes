package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HobgoblinMantledMarauder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BagelAndSchmear.class, HobgoblinMantledMarauder.class})
class BagelAndSchmearTest extends BaseCardTest {

    @Test
    @DisplayName("Share sacrifices the artifact, counters a creature, and draws a card")
    void shareCountersCreatureAndDraws() {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HobgoblinMantledMarauder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bagel);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Share can be activated without a target and still draws a card")
    void shareCanDeclineCreatureTarget() {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bagel);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Nosh sacrifices the artifact, gains 3 life, and draws a card")
    void noshGainsLifeAndDraws() {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bagel);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void shareCanTargetOpponentsCreatureInPostcombatMainPhase() {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HobgoblinMantledMarauder());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bagel);
        harness.assertInGraveyard(player1, "Bagel and Schmear");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void shareDoesNotDrawWhenChosenTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new BagelAndSchmear());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HobgoblinMantledMarauder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Bagel and Schmear");
        harness.assertInHand(player2, "Hobgoblin, Mantled Marauder");
    }

    @Test
    void shareCannotTargetNoncreatureArtifact() {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bagel.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bagel);
        assertThat(bagel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void shareCannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new BagelAndSchmear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void shareCannotActivateDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new BagelAndSchmear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void shareCannotActivateWithAnAbilityOnStack() {
        harness.addToBattlefield(player1, new BagelAndSchmear());
        harness.addToBattlefield(player1, new BagelAndSchmear());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void noshCanActivateDuringOpponentsUpkeepAndResolvesAfterSacrifice() {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bagel);
        harness.assertInGraveyard(player1, "Bagel and Schmear");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void neitherAbilityCanActivateWithOnlyOneColorlessMana(int abilityIndex) {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bagel);
        assertThat(bagel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void shareCanDeclineTargetEvenWhenCreatureIsAvailable() {
        harness.addToBattlefield(player1, new BagelAndSchmear());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HobgoblinMantledMarauder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HobgoblinMantledMarauder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Bagel and Schmear");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void neitherAbilityCanActivateWhileTapped(int abilityIndex) {
        Permanent bagel = harness.addToBattlefieldAndReturn(player1, new BagelAndSchmear());
        bagel.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bagel);
        assertThat(gd.stack).isEmpty();
    }
}
