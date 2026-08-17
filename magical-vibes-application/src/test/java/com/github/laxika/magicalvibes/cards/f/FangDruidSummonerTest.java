package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FangDruidSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates the optional search prompt")
    void enteringCreatesMayPrompt() {
        setupAndCast();

        resolveCreature();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The search can choose a vanilla creature from the graveyard")
    void searchesGraveyardForVanillaCreature() {
        Card graveyardCreature = new GrizzlyBears();
        Card libraryCreature = new GrizzlyBears();
        Card creatureWithAbility = new LlanowarElves();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        setLibrary(libraryCreature, creatureWithAbility);
        setupAndCast();

        resolveMay(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(libraryCreature.getId(), graveyardCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCreature, creatureWithAbility);
    }

    @Test
    @DisplayName("The search excludes creatures with abilities and takes a library card to hand")
    void searchesLibraryForVanillaCreatureOnly() {
        Card creatureWithAbility = new LlanowarElves();
        Card vanillaCreature = new GrizzlyBears();
        setLibrary(creatureWithAbility, vanillaCreature);
        setupAndCast();

        resolveMay(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(vanillaCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(vanillaCreature.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creatureWithAbility);
    }

    @Test
    @DisplayName("Accepting the search does not find a creature with an ability")
    void doesNotFindCreatureWithAbility() {
        Card creatureWithAbility = new LlanowarElves();
        setLibrary(creatureWithAbility);
        setupAndCast();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creatureWithAbility);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creatureWithAbility);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new FangDruidSummoner()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveCreature() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveMay(boolean choice) {
        resolveCreature();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
