package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrackedSkull.class, Forest.class, GrizzlyBears.class, HillGiant.class, Peek.class, Shock.class})
class CrackedSkullTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield reveals the target player's hand and offers a nonland discard")
    void etbOffersNonlandDiscard() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));
        castAndResolveAura(enchanted);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Declining the optional discard leaves the target player's hand unchanged")
    void decliningDiscardLeavesHandUnchanged() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        List<Card> hand = setTargetHand(new Peek(), new Forest());
        castAndResolveAura(enchanted);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(hand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage to the enchanted creature destroys it")
    void damageToEnchantedCreatureDestroysIt() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        setTargetHand(new Peek());
        castAndResolveAura(enchanted);
        harness.handleCardChosen(player1, -1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Cracked Skull"));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Cracked Skull");
    }

    @Test
    @DisplayName("Damage to a different creature does not destroy the enchanted creature")
    void damageToDifferentCreatureDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        setTargetHand(new Peek());
        castAndResolveAura(enchanted);
        harness.handleCardChosen(player1, -1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, other.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Cracked Skull");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void castAndResolveAura(Permanent enchanted) {
        harness.setHand(player1, List.of(new CrackedSkull()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, List.of(enchanted.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> setTargetHand(Card... cards) {
        List<Card> hand = new ArrayList<>(List.of(cards));
        harness.setHand(player2, hand);
        return hand;
    }
}
