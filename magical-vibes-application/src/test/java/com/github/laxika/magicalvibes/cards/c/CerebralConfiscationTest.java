package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CerebralConfiscation.class, Forest.class, GrizzlyBears.class})
class CerebralConfiscationTest extends BaseCardTest {

    @Test
    @DisplayName("Discard-two mode makes the target opponent choose two cards")
    void discardTwoMode() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new CerebralConfiscation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Hand mode lets the caster choose a nonland card to discard")
    void handModeChoosesNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new CerebralConfiscation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Both modes can target only an opponent")
    void bothModesRejectTheCasterAsTarget() {
        harness.setHand(player1, List.of(new CerebralConfiscation()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
