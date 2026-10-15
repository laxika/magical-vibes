package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.b.BranchingBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiftOfTheGargantuan.class, CylianElf.class, DruidOfTheAnima.class, BranchingBolt.class, Forest.class, Plains.class})
class GiftOfTheGargantuanTest extends BaseCardTest {

    @Test
    @DisplayName("First pick offers only the creature cards among the top four")
    void firstPickOffersCreatures() {
        setupTopFour(new CylianElf(), new BranchingBolt(), new Forest(), new DruidOfTheAnima());

        resolveGift();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Cylian Elf", "Druid of the Anima");
    }

    @Test
    @DisplayName("Revealing a creature and a land puts both into hand")
    void revealsCreatureAndLand() {
        setupTopFour(new CylianElf(), new BranchingBolt(), new Forest(), new DruidOfTheAnima());

        resolveGift();

        GameData gd = harness.getGameData();
        // Pick the creature (Cylian Elf).
        harness.handleCardChosen(player1, 0);

        // Second pick offers only the land.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Forest");

        // Pick the land (Forest).
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .contains("Cylian Elf", "Forest");
        // The rest are bottomed in any order.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Cannot take two creatures â€” the second pick is land-only")
    void cannotTakeTwoCreatures() {
        setupTopFour(new CylianElf(), new BranchingBolt(), new Forest(), new DruidOfTheAnima());

        resolveGift();

        GameData gd = harness.getGameData();
        // Pick the first creature.
        harness.handleCardChosen(player1, 0);

        // The follow-up pick never offers the other creature.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .doesNotContain("Druid of the Anima")
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Declining the creature still offers the land pick")
    void decliningCreatureStillOffersLand() {
        setupTopFour(new CylianElf(), new BranchingBolt(), new Forest(), new DruidOfTheAnima());

        resolveGift();

        GameData gd = harness.getGameData();
        // Decline the creature.
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("With no creatures, the land pick begins directly")
    void noCreaturesGoesStraightToLand() {
        setupTopFour(new BranchingBolt(), new Forest(), new Plains(), new BranchingBolt());

        resolveGift();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Forest", "Plains");
    }

    @Test
    @DisplayName("With no creatures or lands, the looked-at cards are bottomed directly")
    void noEligibleBottomsDirectly() {
        setupTopFour(new BranchingBolt(), new BranchingBolt(), new BranchingBolt(), new BranchingBolt());

        resolveGift();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    void putsUnchosenCardsBelowUntouchedLibraryInChosenOrder() {
        Card creature = new CylianElf();
        Card spell = new BranchingBolt();
        Card land = new Forest();
        Card otherCreature = new DruidOfTheAnima();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(creature, spell, land, otherCreature, untouched));

        resolveGift();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, otherCreature, spell);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Gift of the Gargantuan");
    }

    @Test
    void mayDeclineBothCardsAndOrderAllFourOnBottom() {
        Card creature = new CylianElf();
        Card spell = new BranchingBolt();
        Card land = new Forest();
        Card otherCreature = new DruidOfTheAnima();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(creature, spell, land, otherCreature, untouched));

        resolveGift();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, otherCreature, land, spell, creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayTakeOnlyCreatureAndDeclineLand() {
        Card creature = new CylianElf();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        resolveGift();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayTakeOnlyLandAfterDecliningCreature() {
        Card creature = new CylianElf();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        resolveGift();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void shortLibraryWithNoLandOffersOnlyOneCreature() {
        Card creature = new CylianElf();
        Card otherCreature = new DruidOfTheAnima();
        harness.setLibrary(player1, List.of(creature, otherCreature));

        resolveGift();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void withOnlyLandsTakesAtMostOne() {
        Card forest = new Forest();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));

        resolveGift();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryCompletesWithoutChoices() {
        harness.setLibrary(player1, List.of());

        resolveGift();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Gift of the Gargantuan");
    }

    private void resolveGift() {
        harness.setHand(player1, List.of(new GiftOfTheGargantuan()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setupTopFour(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
