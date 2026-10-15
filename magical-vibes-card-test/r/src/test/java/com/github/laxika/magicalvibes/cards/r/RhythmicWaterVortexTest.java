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
        harness.assertNotInGraveyard(player1, "Mu Yanling");
    }

    @Test
    @DisplayName("Returns one creature even when no Mu Yanling can be found")
    void returnsOneCreatureWithoutSearchMatch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setGraveyard(player1, List.of());

        cast(List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Rhythmic Water Vortex");
        harness.assertNotInHand(player1, "Mu Yanling");
    }

    @Test
    @DisplayName("Finds only one Mu Yanling when both search zones contain a copy")
    void choosesOneCardAcrossBothZones() {
        MuYanling libraryCopy = new MuYanling();
        MuYanling graveyardCopy = new MuYanling();
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setGraveyard(player1, List.of(graveyardCopy));

        cast(List.of());

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(libraryCopy.getId(), graveyardCopy.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(libraryCopy.getId(), graveyardCopy.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCopy);
        harness.assertNotInGraveyard(player1, "Mu Yanling");
    }

    @Test
    @DisplayName("May fail to find Mu Yanling when searching the library")
    void mayFailToFindInLibrary() {
        MuYanling muYanling = new MuYanling();
        harness.setLibrary(player1, List.of(muYanling));
        harness.setGraveyard(player1, List.of());

        cast(List.of());
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotInHand(player1, "Mu Yanling");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(muYanling);
        harness.assertInGraveyard(player1, "Rhythmic Water Vortex");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not search when its only target leaves before resolution")
    void doesNotSearchWhenAllTargetsAreIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MuYanling muYanling = new MuYanling();
        harness.setLibrary(player1, List.of(muYanling));
        harness.setHand(player1, List.of(new RhythmicWaterVortex()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class))
                .isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(muYanling);
        harness.assertNotInHand(player1, "Mu Yanling");
        harness.assertInGraveyard(player1, "Rhythmic Water Vortex");
    }

    @Test
    @DisplayName("Returns the remaining legal target and searches when one target leaves")
    void resolvesWithOneRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MuYanling muYanling = new MuYanling();
        harness.setLibrary(player1, List.of(muYanling));
        harness.setHand(player1, List.of(new RhythmicWaterVortex()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(muYanling.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Mu Yanling");
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new RhythmicWaterVortex()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
