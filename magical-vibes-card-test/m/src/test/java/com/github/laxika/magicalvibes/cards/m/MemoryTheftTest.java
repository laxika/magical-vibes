package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FoulmireKnight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LostLegion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MemoryTheft.class, FoulmireKnight.class, Forest.class, LostLegion.class})
class MemoryTheftTest extends BaseCardTest {

    @Test
    void discardsChosenNonlandAndOffersOnlyAdventureCardsFromExile() {
        FoulmireKnight exiledAdventure = new FoulmireKnight();
        LostLegion exiledNonAdventure = new LostLegion();
        harness.setHand(player2, List.of(new LostLegion(), new Forest()));
        harness.setExile(player2, List.of(exiledAdventure, exiledNonAdventure));
        castMemoryTheft();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.FaceUpExiledCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.FaceUpExiledCardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(exiledAdventure.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiledAdventure.getId()));

        harness.assertInGraveyard(player2, "Lost Legion");
        harness.assertInGraveyard(player2, "Foulmire Knight");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactly(exiledNonAdventure.getId());
    }

    @Test
    void decliningAdventureExileChoiceLeavesExileUnchanged() {
        FoulmireKnight exiledAdventure = new FoulmireKnight();
        harness.setHand(player2, List.of(new LostLegion(), new Forest()));
        harness.setExile(player2, List.of(exiledAdventure));
        castMemoryTheft();

        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player2, "Lost Legion");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactly(exiledAdventure.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noAdventureCardInExileDoesNotOpenAChoice() {
        harness.setHand(player2, List.of(new LostLegion(), new Forest()));
        harness.setExile(player2, List.of(new LostLegion()));
        castMemoryTheft();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Lost Legion");
        assertThat(gd.exiledCards).hasSize(1);
    }

    @Test
    void emptyHandStillAllowsMovingAnAdventureFromExile() {
        FoulmireKnight adventure = new FoulmireKnight();
        harness.setHand(player2, List.of());
        harness.setExile(player2, List.of(adventure));

        castMemoryTheft();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.FaceUpExiledCardChoice.class)
                .validCardIds()).containsExactly(adventure.getId());
        harness.handleMultipleCardsChosen(player1, List.of(adventure.getId()));
        harness.assertInGraveyard(player2, "Foulmire Knight");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void landOnlyHandStillAllowsMovingAnAdventureFromExile() {
        FoulmireKnight adventure = new FoulmireKnight();
        harness.setHand(player2, List.of(new Forest()));
        harness.setExile(player2, List.of(adventure));

        castMemoryTheft();

        harness.handleMultipleCardsChosen(player1, List.of(adventure.getId()));
        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Foulmire Knight");
    }

    @Test
    void exileChoiceExcludesFaceDownCardsAndCardsOwnedByCaster() {
        FoulmireKnight eligible = new FoulmireKnight();
        FoulmireKnight faceDown = new FoulmireKnight();
        FoulmireKnight casterOwned = new FoulmireKnight();
        harness.setHand(player2, List.of());
        harness.setExile(player2, List.of(eligible));
        gd.addToExile(player2.getId(), faceDown, null, true);
        gd.addToExile(player1.getId(), casterOwned);

        castMemoryTheft();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.FaceUpExiledCardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(faceDown.getId(), casterOwned.getId());
        harness.assertInGraveyard(player2, "Foulmire Knight");
        harness.assertNotInGraveyard(player1, "Foulmire Knight");
    }

    private void castMemoryTheft() {
        harness.setHand(player1, List.of(new MemoryTheft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
