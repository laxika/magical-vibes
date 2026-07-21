package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellboundDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking draws, discards, and pumps power by the discarded card's mana value")
    void pumpsByDiscardedManaValue() {
        Permanent dragon = attackWithDragon(List.of(new GrizzlyBears()), new Cancel());

        // Discard Grizzly Bears (mana value 2) → +2/+0 until end of turn.
        discardByName(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5); // 3 base + 2
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5); // toughness unchanged
    }

    @Test
    @DisplayName("Discarding a zero-mana-value card grants no boost")
    void zeroManaValueNoBoost() {
        Permanent dragon = attackWithDragon(List.of(new Mountain()), new Cancel());

        // Discard a Mountain (mana value 0) → +0/+0.
        discardByName(player1, "Mountain");

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(3);
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void boostWearsOff() {
        Permanent dragon = attackWithDragon(List.of(new GrizzlyBears()), new Cancel());
        discardByName(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(3);
    }

    /**
     * Puts a ready Spellbound Dragon onto the battlefield with the given hand and a card to draw,
     * declares it as the sole attacker, and resolves the attack trigger up to the discard choice.
     */
    private Permanent attackWithDragon(List<Card> hand, Card cardToDraw) {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SpellboundDragon());
        dragon.setSummoningSick(false);

        harness.setHand(player1, hand);
        gd.playerDecks.get(player1.getId()).add(cardToDraw);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int dragonIdx = gd.playerBattlefields.get(player1.getId()).indexOf(dragon);
        gs.declareAttackers(gd, player1, List.of(dragonIdx), null);

        // Resolve the attack trigger: draws a card, then begins the discard choice.
        harness.passBothPriorities();
        return dragon;
    }

    private void discardByName(Player player, String cardName) {
        List<Card> hand = gd.playerHands.get(player.getId());
        int idx = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                idx = i;
                break;
            }
        }
        assertThat(idx).as("card '%s' present in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player, idx);
    }
}
