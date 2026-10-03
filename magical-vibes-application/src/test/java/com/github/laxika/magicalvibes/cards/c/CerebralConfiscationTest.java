package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CerebralConfiscation.class, Forest.class, GrizzlyBears.class})
class CerebralConfiscationTest extends BaseCardTest {

    @Test
    @DisplayName("Discard-two mode makes the target opponent choose two cards")
    void discardTwoMode() {
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears()));
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
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
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

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mode resolves against an opponent with an empty hand")
    void emptyHand(int mode) {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new CerebralConfiscation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, mode, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cerebral Confiscation");
    }

    @Test
    @DisplayName("Discard-two mode discards the only card when fewer than two are available")
    void discardTwoWithOneCard() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new CerebralConfiscation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hand mode leaves an all-land hand intact without requiring a choice")
    void handModeWithOnlyLands() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new CerebralConfiscation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cerebral Confiscation");
    }

    @Test
    @DisplayName("Hand mode requires the caster to choose a nonland card, including a sorcery")
    void handModeCannotChooseLandOrDecline() {
        harness.setHand(player2, List.of(new Forest(), new CerebralConfiscation()));
        harness.setHand(player1, List.of(new CerebralConfiscation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Cerebral Confiscation");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
