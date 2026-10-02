package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivinerOfFates.class, FaithlessLooting.class, GrizzlyBears.class, Island.class, Mountain.class})
class DivinerOfFatesTest extends BaseCardTest {

    @Test
    void entersAndConnivesThenSeeksMatchingCardType() {
        harness.setHand(player1, List.of(new DivinerOfFates(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addDivinerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent diviner = findPermanent(player1, "Diviner of Fates");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mountain", "Grizzly Bears");
    }

    @Test
    void seeksOnlyOncePerTurnForDiscardEvents() {
        FaithlessLooting firstLooting = new FaithlessLooting();
        FaithlessLooting secondLooting = new FaithlessLooting();
        harness.addToBattlefield(player1, new DivinerOfFates());
        harness.setHand(player1, List.of(firstLooting, secondLooting));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Mountain(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        int secondLootingIndex = findCardIndex(player1, secondLooting);
        harness.castSorcery(player1, secondLootingIndex, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mountain");
    }

    private void addDivinerMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private int findCardIndex(Player player, Card card) {
        List<Card> hand = gd.playerHands.get(player.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(card.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card is not in hand");
    }
}
