package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlitterwingNuisance.class, GrizzlyBears.class, Forest.class, ElspethKnightErrant.class})
class FlitterwingNuisanceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a -1/-1 counter")
    void entersWithMinusOneMinusOneCounter() {
        harness.setHand(player1, List.of(new FlitterwingNuisance()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent flitterwing = findPermanent(player1, "Flitterwing Nuisance");
        assertThat(flitterwing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing a counter makes each creature's combat damage draw a card")
    void drawsForEachCreatureDealingCombatDamage() {
        Permanent flitterwing = addReadyFlitterwing();
        flitterwing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(flitterwing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat + 2);
    }

    @Test
    @DisplayName("Combat damage to a planeswalker also draws a card")
    void drawsForCombatDamageToPlaneswalker() {
        Permanent flitterwing = addReadyFlitterwing();
        flitterwing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        resolveCombat();
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat + 1);
    }

    @Test
    @DisplayName("Cannot remove a counter when the creature has none")
    void cannotActivateWithoutCounter() {
        addReadyFlitterwing();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("The controller chooses which kind of counter to remove")
    void choosesCounterTypeWhenSeveralKindsArePresent() {
        Permanent flitterwing = addReadyFlitterwing();
        flitterwing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        flitterwing.setCounterCount(CounterType.OIL, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(flitterwing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(flitterwing.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("A +1/+1 counter can pay the cost even while summoning sick")
    void removesPlusCounterWhileSummoningSick() {
        Permanent flitterwing = addReadyFlitterwing();
        flitterwing.setSummoningSick(true);
        flitterwing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(flitterwing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The delayed draw trigger persists after Flitterwing leaves")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent flitterwing = addReadyFlitterwing();
        flitterwing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        addCreatureReady(player1, new FlitterwingNuisance());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(flitterwing);
        gd.playerGraveyards.get(player1.getId()).add(flitterwing.getCard());

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat + 1);
    }
    private Permanent addReadyFlitterwing() {
        Permanent flitterwing = addCreatureReady(player1, new FlitterwingNuisance());
        return flitterwing;
    }
}
