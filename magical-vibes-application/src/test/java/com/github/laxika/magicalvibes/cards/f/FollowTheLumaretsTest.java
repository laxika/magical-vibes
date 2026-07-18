package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FollowTheLumaretsTest extends BaseCardTest {

    

    @Test
    @DisplayName("Without gaining life, offers a single-pick of the creature/land cards among top four")
    void baseModeOffersSinglePick() {
        setupTopFour(List.of(new LlanowarElves(), new Shock(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Forest");
    }

    @Test
    @DisplayName("After gaining life, offers a multi-pick capped at two creature/land cards")
    void infusionModeOffersMultiPickCappedAtTwo() {
        setupTopFour(List.of(new LlanowarElves(), new GrizzlyBears(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        setupTopFour(List.of(elves, new Shock(), forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId(), forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Llanowar Elves", "Forest");
    }

    @Test
    @DisplayName("Base mode puts a single chosen card into hand and reorders the rest to bottom")
    void baseModePutsOneCardIntoHand() {
        LlanowarElves elves = new LlanowarElves();
        setupTopFour(List.of(elves, new Shock(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("With empty library, Follow the Lumarets does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new FollowTheLumarets()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopFour(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
