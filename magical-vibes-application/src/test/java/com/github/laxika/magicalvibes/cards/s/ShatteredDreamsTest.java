package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CacklingImp;
import com.github.laxika.magicalvibes.cards.c.Condescend;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ShatteredDreams.class, ConjurersBauble.class, Condescend.class, CacklingImp.class})
class ShatteredDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals an opponent's hand and allows choosing an artifact")
    void promptsForArtifactChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new ConjurersBauble(), new Condescend())));
        castShatteredDreams();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Discards the chosen artifact")
    void discardsChosenArtifact() {
        harness.setHand(player2, new ArrayList<>(List.of(new ConjurersBauble(), new Condescend())));
        castShatteredDreams();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Conjurer's Bauble");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Condescend");
    }

    @Test
    @DisplayName("Discards an artifact chosen at a later hand index")
    void discardsArtifactAtLaterHandIndex() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new Condescend(), new ConjurersBauble(), new CacklingImp())));
        castShatteredDreams();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Conjurer's Bauble");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Condescend", "Cackling Imp");
    }

    @Test
    @DisplayName("Non-artifact cards cannot be chosen")
    void nonArtifactCardsAreExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new Condescend(), new CacklingImp())));
        castShatteredDreams();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new ShatteredDreams()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShatteredDreams() {
        harness.setHand(player1, List.of(new ShatteredDreams()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
