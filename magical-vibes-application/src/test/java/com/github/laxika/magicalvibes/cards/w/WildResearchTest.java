package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LlanowarDead;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.p.PropheticBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildResearch.class, PhyrexianArena.class, PropheticBolt.class, LlanowarDead.class})
class WildResearchTest extends BaseCardTest {

    @Test
    @DisplayName("White ability searches for an enchantment, then discards it at random")
    void whiteAbilitySearchesForEnchantmentThenDiscards() {
        PendingInteraction.LibrarySearch search = activate(0, ManaColor.WHITE,
                List.of(new PhyrexianArena(), new PropheticBolt(), new LlanowarDead()));

        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Phyrexian Arena");
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.ENCHANTMENT));

        chooseFoundCard();

        harness.assertInGraveyard(player1, "Phyrexian Arena");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Prophetic Bolt", "Llanowar Dead");
    }

    @Test
    @DisplayName("Blue ability searches for an instant, then discards it at random")
    void blueAbilitySearchesForInstantThenDiscards() {
        PendingInteraction.LibrarySearch search = activate(1, ManaColor.BLUE,
                List.of(new PhyrexianArena(), new PropheticBolt(), new LlanowarDead()));

        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Prophetic Bolt");
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.INSTANT));

        chooseFoundCard();

        harness.assertInGraveyard(player1, "Prophetic Bolt");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Phyrexian Arena", "Llanowar Dead");
    }

    @Test
    @DisplayName("White ability randomly discards from the hand after finding an enchantment")
    void whiteAbilityRandomlyDiscardsFromResultingHand() {
        activate(0, ManaColor.WHITE,
                List.of(new LlanowarDead()),
                List.of(new PhyrexianArena(), new PropheticBolt()));

        chooseFoundCard();

        List<Card> hand = gd.playerHands.get(player1.getId());
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(graveyard).hasSize(1);
        assertThat(List.of(hand.getFirst().getName(), graveyard.getFirst().getName()))
                .containsExactlyInAnyOrder("Llanowar Dead", "Phyrexian Arena");
    }

    @Test
    @DisplayName("White ability still discards and shuffles when no enchantment is found")
    void whiteAbilityStillDiscardsAndShufflesWhenNoEnchantmentIsFound() {
        activate(0, ManaColor.WHITE,
                List.of(new LlanowarDead()),
                List.of(new PropheticBolt(), new LlanowarDead()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Llanowar Dead");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Prophetic Bolt", "Llanowar Dead");
    }

    private PendingInteraction.LibrarySearch activate(int abilityIndex, ManaColor coloredMana,
                                                       List<Card> library) {
        return activate(abilityIndex, coloredMana, List.of(), library);
    }

    private PendingInteraction.LibrarySearch activate(int abilityIndex, ManaColor coloredMana,
                                                       List<Card> hand, List<Card> library) {
        harness.addToBattlefield(player1, new WildResearch());
        harness.setHand(player1, hand);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, coloredMana, 1);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseFoundCard() {
        harness.handleCardChosen(player1, 0);
    }
}
