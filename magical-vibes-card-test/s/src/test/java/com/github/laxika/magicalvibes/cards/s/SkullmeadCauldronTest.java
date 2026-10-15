package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullmeadCauldron.class, MistralCharger.class})
class SkullmeadCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping gains 1 life")
    void tappingGainsOneLife() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new SkullmeadCauldron());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(cauldron.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Discarding a card and tapping gains 3 life")
    void discardingCardAndTappingGainsThreeLife() {
        harness.addToBattlefield(player1, new SkullmeadCauldron());
        harness.setHand(player1, List.of(new MistralCharger(), new MistralCharger()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Mistral Charger");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Mistral Charger");
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

    @Test
    @DisplayName("Discard and tap are paid before life is gained")
    void discardAndTapArePaidBeforeResolution() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new SkullmeadCauldron());
        harness.setHand(player1, List.of(new SkullmeadCauldron()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(cauldron.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Skullmead Cauldron");
        harness.assertInGraveyard(player1, "Skullmead Cauldron");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Both abilities require an untapped cauldron")
    void bothAbilitiesRequireUntappedCauldron() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new SkullmeadCauldron());
        harness.setHand(player1, List.of(new MistralCharger()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        cauldron.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mistral Charger");
        harness.assertNotInGraveyard(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("The first ability works with an empty hand during the opponent's turn")
    void gainsLifeDuringOpponentsTurnWithoutDiscard() {
        harness.addToBattlefield(player1, new SkullmeadCauldron());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
