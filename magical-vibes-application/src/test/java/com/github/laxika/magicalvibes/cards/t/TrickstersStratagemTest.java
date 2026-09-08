package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrickstersStratagem.class, GrizzlyBears.class, Island.class})
class TrickstersStratagemTest extends BaseCardTest {

    @Test
    void ownerKeepsCreatureSecondFromTopThenControlledCreatureConnives() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new Island();
        Card bottomCard = new Island();
        setDeck(player2, List.of(topCard, bottomCard));
        setDeck(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TrickstersStratagem(), new Island()));
        addMana();

        harness.castSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, opponentCreature.getCard(), bottomCard);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void ownerPutsCreatureOnBottomAndConniveTargetMayBeOmitted() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new Island();
        setDeck(player2, List.of(topCard));
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        harness.castSorcery(player1, 0, List.of(opponentCreature.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, opponentCreature.getCard());
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void requiresOpponentCreatureAsFirstTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresControlledCreatureAsOptionalSecondTarget() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrickstersStratagem()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opponentCreature.getId(), otherOpponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
