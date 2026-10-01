package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({UrborgSyphonMage.class, Swamp.class})
class UrborgSyphonMageTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card makes each other player lose 2 life and gains that much life")
    void drainsEachOtherPlayer() {
        Permanent mage = addCreatureReady(player1, new UrborgSyphonMage());
        Swamp discarded = new Swamp();
        harness.setHand(player1, List.of(discarded));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void requiresCardToDiscard() {
        Permanent mage = addCreatureReady(player1, new UrborgSyphonMage());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated while the mage is tapped")
    void cannotActivateWhenTapped() {
        Permanent mage = addCreatureReady(player1, new UrborgSyphonMage());
        mage.tap();
        Swamp discarded = new Swamp();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(mage.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
    }
}
