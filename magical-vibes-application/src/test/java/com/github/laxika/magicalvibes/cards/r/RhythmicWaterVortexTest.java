package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MuYanling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhythmicWaterVortex.class, GrizzlyBears.class, Island.class, MuYanling.class})
class RhythmicWaterVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two creatures and searches the library for Mu Yanling")
    void returnsCreaturesAndSearchesLibrary() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MuYanling muYanling = new MuYanling();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), muYanling));
        cast(List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> "Grizzly Bears".equals(card.getName()))
                .hasSize(2);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(muYanling.getId());

        harness.handleMultipleCardsChosen(player1, List.of(muYanling.getId()));

        harness.assertInHand(player1, "Mu Yanling");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> "Mu Yanling".equals(card.getName()));
    }

    @Test
    @DisplayName("Searches the graveyard for Mu Yanling")
    void searchesGraveyard() {
        MuYanling muYanling = new MuYanling();
        harness.setGraveyard(player1, List.of(muYanling));
        cast(List.of());

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(muYanling.getId());

        harness.handleMultipleCardsChosen(player1, List.of(muYanling.getId()));

        harness.assertInHand(player1, "Mu Yanling");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> "Mu Yanling".equals(card.getName()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new RhythmicWaterVortex()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
