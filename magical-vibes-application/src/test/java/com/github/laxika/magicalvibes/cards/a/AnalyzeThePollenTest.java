package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnalyzeThePollen.class, Forest.class, GhostQuarter.class, GrizzlyBears.class, Shock.class})
class AnalyzeThePollenTest extends BaseCardTest {

    @Test
    @DisplayName("Without collected evidence, offers only basic lands")
    void withoutEvidenceOffersOnlyBasicLands() {
        setupLibrary();
        cast(List.of());
        harness.passBothPriorities();

        List<Card> offered = librarySearch().params().cards();
        assertThat(offered).extracting(Card::getClass).containsExactly(Forest.class);
    }

    @Test
    @DisplayName("Collected evidence offers creatures and lands")
    void withEvidenceOffersCreaturesAndLands() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        setupLibrary();
        cast(List.of(0, 1, 2, 3));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);

        harness.passBothPriorities();

        assertThat(librarySearch().params().cards()).extracting(Card::getClass)
                .containsExactlyInAnyOrder(Forest.class, GhostQuarter.class, GrizzlyBears.class);
    }

    @Test
    @DisplayName("Collected evidence can tutor a creature")
    void withEvidenceCanTutorCreature() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        setupLibrary();
        cast(List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        List<Card> offered = librarySearch().params().cards();
        Card creature = offered.stream()
                .filter(card -> card instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        int creatureIndex = offered.indexOf(creature);
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(creatureIndex));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    private void cast(List<Integer> evidenceIndices) {
        harness.setHand(player1, List.of(new AnalyzeThePollen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, evidenceIndices);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new GhostQuarter(), new GrizzlyBears(), new Shock()));
    }

    private PendingInteraction.LibrarySearch librarySearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
