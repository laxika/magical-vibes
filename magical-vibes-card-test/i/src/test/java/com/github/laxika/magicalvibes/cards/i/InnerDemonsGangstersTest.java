package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InnerDemonsGangsters.class})
class InnerDemonsGangstersTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card gives +1/+0 and menace until end of turn")
    void discardBoostsAndGrantsMenace() {
        forceMainPhase();
        Permanent gangsters = harness.addToBattlefieldAndReturn(player1, new InnerDemonsGangsters());
        harness.setHand(player1, List.of(new InnerDemonsGangsters()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gangsters.getPowerModifier()).isEqualTo(1);
        assertThat(gangsters.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, gangsters, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Inner Demons Gangsters");
    }

    @Test
    @DisplayName("The boost and menace wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        forceMainPhase();
        Permanent gangsters = harness.addToBattlefieldAndReturn(player1, new InnerDemonsGangsters());
        harness.setHand(player1, List.of(new InnerDemonsGangsters()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gangsters.getPowerModifier()).isZero();
        assertThat(gangsters.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, gangsters, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        forceMainPhase();
        harness.addToBattlefieldAndReturn(player1, new InnerDemonsGangsters());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot be activated outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new InnerDemonsGangsters());
        harness.setHand(player1, List.of(new InnerDemonsGangsters()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard is paid before the boost and menace resolve")
    void discardIsPaidBeforeResolution() {
        forceMainPhase();
        Permanent gangsters = harness.addToBattlefieldAndReturn(player1, new InnerDemonsGangsters());
        harness.setHand(player1, List.of(new InnerDemonsGangsters()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Inner Demons Gangsters");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gangsters.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, gangsters, Keyword.MENACE)).isFalse();

        harness.passBothPriorities();

        assertThat(gangsters.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, gangsters, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Repeated activations stack the boost even while tapped and summoning sick")
    void repeatedActivationsStackBoost() {
        forceMainPhase();
        Permanent gangsters = harness.addToBattlefieldAndReturn(player1, new InnerDemonsGangsters());
        gangsters.tap();
        gangsters.setSummoningSick(true);
        harness.setHand(player1, List.of(new InnerDemonsGangsters(), new InnerDemonsGangsters()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gangsters.getPowerModifier()).isEqualTo(2);
        assertThat(gangsters.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, gangsters, Keyword.MENACE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability cannot be activated in response to itself")
    void cannotActivateWithNonemptyStack() {
        forceMainPhase();
        harness.addToBattlefield(player1, new InnerDemonsGangsters());
        harness.setHand(player1, List.of(new InnerDemonsGangsters(), new InnerDemonsGangsters()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The ability cannot be activated during an opponent's main phase")
    void cannotActivateDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new InnerDemonsGangsters());
        harness.setHand(player1, List.of(new InnerDemonsGangsters()));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
