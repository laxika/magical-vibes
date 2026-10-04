package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntersInsight.class, RuneclawBear.class, Forest.class, GarrukPrimalHunter.class, ActOfTreason.class})
class HuntersInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the combat damage the chosen creature deals to a player")
    void drawsEqualToCombatDamage() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        bears.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        // Runeclaw Bear dealt 2 combat damage, so two cards are drawn.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("The delayed trigger expires at end of turn")
    void triggerWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        bears.setAttacking(true);
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws for combat damage dealt to a planeswalker")
    void drawsForPlaneswalkerCombatDamage() {
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukPrimalHunter());
        garruk.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        bear.setAttacking(true);
        bear.setAttackTarget(garruk.getId());
        resolveCombat();
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("The spell controller draws even after the chosen creature changes controller")
    void originalSpellControllerDrawsAfterControlChange() {
        harness.forceActivePlayer(player2);
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, bear.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        int originalControllerHand = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHand = gd.playerHands.get(player2.getId()).size();

        bear.setAttacking(true);
        resolveCombat(player2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(originalControllerHand + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHand);
    }

}
