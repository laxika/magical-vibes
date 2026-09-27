package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeTragedies.class, GnarledMass.class, FrostOgre.class})
class ThreeTragediesTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards three cards of their choice")
    void targetDiscardsThreeCards() {
        harness.setHand(player2, List.of(new GnarledMass(), new FrostOgre(), new GnarledMass(), new FrostOgre()));
        harness.setHand(player1, List.of(new ThreeTragedies()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(3);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Frost Ogre");
        harness.assertInGraveyard(player1, "Three Tragedies");
    }

    @Test
    @DisplayName("Target with fewer than three cards discards their whole hand")
    void targetWithFewerCardsDiscardsAll() {
        harness.setHand(player2, List.of(new GnarledMass(), new GnarledMass()));
        harness.setHand(player1, List.of(new ThreeTragedies()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Target with exactly three cards discards their whole hand")
    void targetWithExactlyThreeCardsDiscardsAll() {
        harness.setHand(player2, List.of(new GnarledMass(), new GnarledMass(), new GnarledMass()));
        harness.setHand(player1, List.of(new ThreeTragedies()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The caster can be the targeted player")
    void casterCanBeTargeted() {
        harness.setHand(player1, List.of(
                new ThreeTragedies(), new GnarledMass(), new GnarledMass(), new GnarledMass()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Target with an empty hand is not prompted")
    void targetWithEmptyHandNoPrompt() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ThreeTragedies()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
