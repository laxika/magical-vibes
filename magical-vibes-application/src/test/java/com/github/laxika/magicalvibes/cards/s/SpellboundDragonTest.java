package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellboundDragon.class, GrizzlyBears.class, Cancel.class, Mountain.class, StinkweedImp.class})
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

    @Test
    @DisplayName("An empty hand can discard the card just drawn")
    void discardsNewlyDrawnCardFromEmptyHand() {
        SpellboundDragon drawn = new SpellboundDragon();
        harness.setLibrary(player1, List.of());
        Permanent dragon = attackWithDragon(List.of(), drawn);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
    }

    @Test
    @DisplayName("Dredge finishes before the attack trigger asks for a discard")
    void dredgesBeforeDiscarding() {
        Permanent dragon = addCreatureReady(player1, new SpellboundDragon());
        GrizzlyBears held = new GrizzlyBears();
        StinkweedImp imp = new StinkweedImp();
        List<Card> milled = List.of(new SpellboundDragon(), new SpellboundDragon(),
                new SpellboundDragon(), new SpellboundDragon(), new SpellboundDragon());
        harness.setHand(player1, List.of(held));
        harness.setGraveyard(player1, List.of(imp));
        harness.setLibrary(player1, milled);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held, imp);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName(player1, "Stinkweed Imp");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(imp);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
    }

    /**
     * Puts a ready Spellbound Dragon onto the battlefield with the given hand and a card to draw,
     * declares it as the sole attacker, and resolves the attack trigger up to the discard choice.
     */
    private Permanent attackWithDragon(List<Card> hand, Card cardToDraw) {
        Permanent dragon = addCreatureReady(player1, new SpellboundDragon());

        harness.setHand(player1, hand);
        gd.playerDecks.get(player1.getId()).add(cardToDraw);

        int dragonIdx = gd.playerBattlefields.get(player1.getId()).indexOf(dragon);
        declareAttackers(player1, List.of(dragonIdx));

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
