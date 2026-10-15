package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JibbirikOmnivore;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangDruidSummoner.class, GrizzlyBears.class, LlanowarElves.class, JibbirikOmnivore.class, Forest.class})
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
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(libraryCreature, creatureWithAbility);
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

    @Test
    @DisplayName("Declining the search leaves both zones untouched")
    void decliningSearchLeavesBothZonesUntouched() {
        Card libraryCreature = new JibbirikOmnivore();
        Card graveyardCreature = new JibbirikOmnivore();
        setLibrary(libraryCreature);
        harness.setGraveyard(player1, List.of(graveyardCreature));
        setupAndCast();

        resolveMay(false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCreature);
    }

    @Test
    @DisplayName("A library search may fail to find even when a matching card is available")
    void mayFailToFindInLibrary() {
        Card libraryCreature = new JibbirikOmnivore();
        setLibrary(libraryCreature);
        harness.setGraveyard(player1, List.of());
        setupAndCast();

        resolveMay(true);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCreature);
    }

    @Test
    @DisplayName("The search excludes graveyard creatures with abilities and opponents' cards")
    void excludesGraveyardAbilitiesAndOpponentsCards() {
        Card ownCreature = new JibbirikOmnivore();
        Card abilityCreature = new FangDruidSummoner();
        Card opposingCreature = new JibbirikOmnivore();
        setLibrary();
        harness.setGraveyard(player1, List.of(abilityCreature, ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setLibrary(player2, List.of(new JibbirikOmnivore()));
        setupAndCast();

        resolveMay(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(abilityCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    @DisplayName("Noncreature cards are excluded from both search zones")
    void excludesNoncreaturesFromBothZones() {
        Card libraryLand = new Forest();
        Card graveyardLand = new Forest();
        Card creature = new JibbirikOmnivore();
        setLibrary(libraryLand, creature);
        harness.setGraveyard(player1, List.of(graveyardLand));
        setupAndCast();

        resolveMay(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardLand);
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
        harness.setLibrary(player1, List.of(cards));
    }
}
