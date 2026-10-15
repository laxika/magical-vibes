package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PutridLeech;
import com.github.laxika.magicalvibes.cards.c.ColossalMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.FieldmistBorderpost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brainbite.class, PutridLeech.class, ColossalMight.class, FieldmistBorderpost.class})
class BrainbiteTest extends BaseCardTest {

    @Test
    @DisplayName("Caster chooses a card to discard, then draws a card")
    void discardsChosenCardAndDraws() {
        harness.setHand(player2, List.of(new PutridLeech(), new ColossalMight()));
        harness.setLibrary(player1, List.of(new PutridLeech()));

        harness.setHand(player1, List.of(new Brainbite()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Putrid Leech");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Colossal Might");
        harness.assertInHand(player1, "Putrid Leech");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws a card even when the target's hand is empty")
    void drawsEvenWithEmptyTargetHand() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new PutridLeech()));
        harness.setHand(player1, List.of(new Brainbite()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Putrid Leech");
    }

    @Test
    @DisplayName("Cannot target self; must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Brainbite(), new PutridLeech()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choice is mandatory, belongs to the caster, and can discard an artifact")
    void mandatoryCasterChoiceCanDiscardArtifact() {
        harness.setHand(player2, List.of(new PutridLeech(), new FieldmistBorderpost()));
        harness.setLibrary(player1, List.of(new ColossalMight(), new PutridLeech()));
        harness.setHand(player1, List.of(new Brainbite()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Fieldmist Borderpost");
        harness.assertInHand(player2, "Putrid Leech");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player1, "Colossal Might");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
