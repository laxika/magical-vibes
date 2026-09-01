package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullmeadCauldron.class, Forest.class, GrizzlyBears.class})
class SkullmeadCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping gains 1 life")
    void tappingGainsOneLife() {
        harness.addToBattlefield(player1, new SkullmeadCauldron());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Discarding a card and tapping gains 3 life")
    void discardingCardAndTappingGainsThreeLife() {
        harness.addToBattlefield(player1, new SkullmeadCauldron());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The discard ability cannot be activated without a card in hand")
    void cannotDiscardWithoutCardInHand() {
        harness.addToBattlefield(player1, new SkullmeadCauldron());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
