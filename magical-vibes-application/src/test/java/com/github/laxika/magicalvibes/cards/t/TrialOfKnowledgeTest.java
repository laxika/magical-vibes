package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CartoucheOfSolidarity;
import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrialOfKnowledge.class, CartoucheOfSolidarity.class, DoomedDissenter.class, Island.class})
class TrialOfKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws three cards, then discards a chosen card")
    void etbDrawsThreeThenDiscardsOne() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new TrialOfKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment (queues ETB trigger)
        harness.passBothPriorities(); // resolve ETB: draw three, then await discard choice

        // After drawing three, the loot awaits a single discard choice.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        // Net: 0 (after cast) + 3 draw - 1 discard = 2 cards; one card in the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returns to hand when a Cartouche you control enters")
    void bouncesWhenAllyCartoucheEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DoomedDissenter());
        harness.addToBattlefield(player1, new TrialOfKnowledge());

        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve aura (queues its ETB + Trial's bounce)
        harness.passBothPriorities(); // resolve a triggered ability
        harness.passBothPriorities(); // resolve the other triggered ability

        harness.assertNotOnBattlefield(player1, "Trial of Knowledge");
        harness.assertInHand(player1, "Trial of Knowledge");
    }

    @Test
    @DisplayName("Does not return when a Cartouche enters under an opponent's control")
    void staysWhenOpponentCartoucheEnters() {
        harness.addToBattlefield(player1, new TrialOfKnowledge());

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DoomedDissenter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, opponentCreature.getId());
        harness.passBothPriorities(); // resolve aura
        harness.passBothPriorities(); // resolve aura's ETB token trigger

        harness.assertOnBattlefield(player1, "Trial of Knowledge");
    }

    @Test
    @DisplayName("May discard a card that was in hand before the draw")
    void canDiscardPreexistingHandCard() {
        Card discard = new DoomedDissenter();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new TrialOfKnowledge(), discard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).doesNotContain(discard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discard);
    }

    @Test
    @DisplayName("A controlled Trial returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        TrialOfKnowledge trial = new TrialOfKnowledge();
        trial.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, trial);
        harness.setHand(player2, List.of());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DoomedDissenter());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Trial of Knowledge");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(trial);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(trial);
    }

    @Test
    @DisplayName("An allied enchantment without the Cartouche subtype does not return the Trial")
    void staysWhenNonCartoucheEnters() {
        TrialOfKnowledge firstTrial = new TrialOfKnowledge();
        TrialOfKnowledge secondTrial = new TrialOfKnowledge();
        harness.addToBattlefield(player1, firstTrial);
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(secondTrial));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(firstTrial, secondTrial);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .doesNotContain(firstTrial, secondTrial);
        assertThat(gd.stack).isEmpty();
    }
}
