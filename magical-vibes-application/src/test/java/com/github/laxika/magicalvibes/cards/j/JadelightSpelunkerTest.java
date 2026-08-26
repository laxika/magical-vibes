package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadelightSpelunker.class, Forest.class, GrizzlyBears.class})
class JadelightSpelunkerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB explores X times and puts revealed lands into hand")
    void exploresXTimesWithLands() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(secondLand);
        gd.playerDecks.get(player1.getId()).addFirst(firstLand);

        castJadelightSpelunker(2);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstLand.getId(), secondLand.getId());
        assertThat(findJadelightSpelunker().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB explores X times and adds a counter for each nonland")
    void exploresXTimesWithNonlands() {
        Card firstNonland = new GrizzlyBears();
        Card secondNonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(secondNonland);
        gd.playerDecks.get(player1.getId()).addFirst(firstNonland);

        castJadelightSpelunker(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findJadelightSpelunker().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstNonland.getId(), secondNonland.getId());
    }

    @Test
    @DisplayName("ETB does not explore when X is zero")
    void doesNotExploreWhenXIsZero() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        castJadelightSpelunker(0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        assertThat(findJadelightSpelunker().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castJadelightSpelunker(int xValue) {
        harness.setHand(player1, List.of(new JadelightSpelunker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        gs.playCard(gd, player1, 0, xValue, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findJadelightSpelunker() {
        return findPermanent(player1, "Jadelight Spelunker");
    }
}
