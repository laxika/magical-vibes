package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FollowTheLumarets.class, LlanowarElves.class, GrizzlyBears.class, Forest.class, Shock.class})
class FollowTheLumaretsTest extends BaseCardTest {

    

    @Test
    @DisplayName("Without gaining life, offers a single-pick of the creature/land cards among top four")
    void baseModeOffersSinglePick() {
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Shock(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Forest");
    }

    @Test
    @DisplayName("After gaining life, offers a multi-pick capped at two creature/land cards")
    void infusionModeOffersMultiPickCappedAtTwo() {
        harness.setLibrary(player1, List.of(new LlanowarElves(), new GrizzlyBears(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).maxCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Infusion mode puts two chosen creature/land cards into hand")
    void infusionModePutsTwoCardsIntoHand() {
        LlanowarElves elves = new LlanowarElves();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(elves, new Shock(), forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId(), forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Llanowar Elves", "Forest");
    }

    @Test
    @DisplayName("Base mode puts a single chosen card into hand and randomly bottoms the rest")
    void baseModePutsOneCardIntoHand() {
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(elves, new Shock(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With empty library, Follow the Lumarets does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void infusionMayChooseOnlyOneAndRandomlyBottomsTheRest() {
        Forest forest = new Forest();
        Shock untouched = new Shock();
        harness.setLibrary(player1, List.of(forest, new LlanowarElves(), new GrizzlyBears(), new Shock(), untouched));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).first().isSameAs(untouched);
    }

    @Test
    void baseModeMayDeclineAllCards() {
        List<Card> cards = List.of(new Forest(), new LlanowarElves(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
    }

    @Test
    void infusionMayDeclineAllCards() {
        List<Card> cards = List.of(new Forest(), new LlanowarElves(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
    }

    @Test
    void noEligibleCardsAreRandomlyBottomedWithoutAChoice() {
        List<Card> cards = List.of(new Shock(), new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
    }

    @Test
    void opponentsLifeGainDoesNotEnableInfusion() {
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.lifeGainedThisTurn.put(player2.getId(), 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void infusionCanTakeBothCardsFromAShortLibrary() {
        Forest forest = new Forest();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(forest, elves));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), elves.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, elves);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
