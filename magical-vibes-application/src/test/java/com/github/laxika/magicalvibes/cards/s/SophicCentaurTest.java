package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SophicCentaur.class)
class SophicCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Gains two life for each card remaining in hand after discarding")
    void gainsLifeForCardsInHand() {
        Permanent centaur = addReadyCentaur();
        harness.setHand(player1, List.of(new SophicCentaur(), new SophicCentaur(), new SophicCentaur()));
        harness.setLife(player1, 10);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(centaur.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Sophic Centaur");
    }

    @Test
    @DisplayName("Counts only cards remaining in the controller's hand")
    void gainsNoLifeWhenDiscardingLastCard() {
        addReadyCentaur();
        harness.setHand(player1, List.of(new SophicCentaur()));
        harness.setHand(player2, List.of(
                new SophicCentaur(), new SophicCentaur(), new SophicCentaur(), new SophicCentaur()));
        harness.setLife(player1, 10);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyCentaur();
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts the hand at resolution after another activation discards a card")
    void countsHandAtResolution() {
        addReadyCentaur();
        addCreatureReady(player1, new SophicCentaur());
        harness.setHand(player1, List.of(new SophicCentaur(), new SophicCentaur(), new SophicCentaur()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        addReadyCentaur().tap();
        harness.setHand(player1, List.of(new SophicCentaur()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        addReadyCentaur().setSummoningSick(true);
        harness.setHand(player1, List.of(new SophicCentaur()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Requires two green mana even when four total mana are available")
    void cannotActivateWithoutEnoughGreenMana() {
        Permanent centaur = addReadyCentaur();
        harness.setHand(player1, List.of(new SophicCentaur()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(centaur.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyCentaur() {
        Permanent centaur = addCreatureReady(player1, new SophicCentaur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return centaur;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 4);
    }
}
