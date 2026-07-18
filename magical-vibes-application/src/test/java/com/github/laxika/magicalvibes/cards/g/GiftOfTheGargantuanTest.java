package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

class GiftOfTheGargantuanTest extends BaseCardTest {

    @Test
    @DisplayName("First pick offers only the creature cards among the top four")
    void firstPickOffersCreatures() {
        setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveGift();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
    }

    @Test
    @DisplayName("Revealing a creature and a land puts both into hand")
    void revealsCreatureAndLand() {
        setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveGift();

        GameData gd = harness.getGameData();
        // Pick the creature (Grizzly Bears).
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Second pick offers only the land.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Forest");

        // Pick the land (Forest).
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Forest");
        // The rest are bottomed in any order.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Cannot take two creatures — the second pick is land-only")
    void cannotTakeTwoCreatures() {
        setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveGift();

        GameData gd = harness.getGameData();
        // Pick the first creature.
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // The follow-up pick never offers the other creature.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .doesNotContain("Hill Giant")
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Declining the creature still offers the land pick")
    void decliningCreatureStillOffersLand() {
        setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveGift();

        GameData gd = harness.getGameData();
        // Decline the creature.
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("With no creatures, the land pick begins directly")
    void noCreaturesGoesStraightToLand() {
        setupTopFour(new Shock(), new Forest(), new Plains(), new Shock());

        resolveGift();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Forest", "Plains");
    }

    @Test
    @DisplayName("With no creatures or lands, the looked-at cards are bottomed directly")
    void noEligibleBottomsDirectly() {
        setupTopFour(new Shock(), new Shock(), new Shock(), new Shock());

        resolveGift();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    private void resolveGift() {
        harness.setHand(player1, List.of(new GiftOfTheGargantuan()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopFour(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
