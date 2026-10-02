package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JetCollector.class, GrizzlyBears.class, HillGiant.class})
class JetCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Mox Jet at the beginning of the second main phase with four cards in the graveyard")
    void conjuresMoxJetAtPostcombatMainWithThreshold() {
        harness.addToBattlefieldAndReturn(player1, new JetCollector());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToPostcombatMain();
        resolveAllTriggers();

        harness.assertInHand(player1, "Mox Jet");
    }

    @Test
    @DisplayName("The conjure trigger requires four graveyard cards and triggers only once")
    void conjureTriggerRequiresThresholdAndIsOnceOnly() {
        Permanent jetCollector = harness.addToBattlefieldAndReturn(player1, new JetCollector());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToPostcombatMain();
        assertThat(gd.stack).isEmpty();

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        advanceToPostcombatMain();
        resolveAllTriggers();
        int handSizeAfterFirstTrigger = gd.playerHands.get(player1.getId()).size();

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterFirstTrigger);
    }

    @Test
    @DisplayName("Returns a creature with mana value X from the graveyard with a finality counter")
    void returnsCreatureWithFinalityCounter() {
        prepareSorceryPhase();
        harness.addToBattlefieldAndReturn(player1, new JetCollector());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(target.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than X")
    void cannotTargetCreatureAboveX() {
        prepareSorceryPhase();
        harness.addToBattlefieldAndReturn(player1, new JetCollector());
        Card target = new HillGiant();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 2, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    private void prepareSorceryPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
