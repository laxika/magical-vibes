package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LunarMysticTest extends BaseCardTest {

    private void seedDeck() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new GrizzlyBears());
    }

    private List<Card> hand() {
        return gd.playerHands.get(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} after casting an instant draws a card")
    void payingDrawsCard() {
        harness.addToBattlefield(player1, new LunarMystic());
        seedDeck();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        int handBefore = hand().size();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the draw on the stack

        assertThat(hand()).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-pay prompt draws nothing")
    void decliningDrawsNothing() {
        harness.addToBattlefield(player1, new LunarMystic());
        seedDeck();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        int handBefore = hand().size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hand()).hasSize(handBefore);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LunarMystic());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
