package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellgorgerBarbarian.class, GrizzlyBears.class, Unsummon.class})
class SpellgorgerBarbarianTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDiscardsACardAtRandom() {
        Card barbarian = new SpellgorgerBarbarian();
        Card cardToDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(barbarian, cardToDiscard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cardToDiscard);
    }

    @Test
    void leavingTheBattlefieldDrawsACard() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player1, new SpellgorgerBarbarian());
        Card cardToDraw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, barbarian.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringWithAnEmptyHandDoesNotRequireADiscard() {
        harness.setHand(player1, List.of(new SpellgorgerBarbarian()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spellgorger Barbarian");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void randomDiscardRemovesExactlyOneCardWithoutAskingForAChoice() {
        Card first = new SpellgorgerBarbarian();
        Card second = new SpellgorgerBarbarian();
        Card opponentCard = new SpellgorgerBarbarian();
        harness.setHand(player1, List.of(new SpellgorgerBarbarian(), first, second));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerHands.get(player1.getId()).getFirst())
                .isNotSameAs(gd.playerGraveyards.get(player1.getId()).getFirst());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dyingDrawsExactlyOneCardForItsController() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new SpellgorgerBarbarian());
        Card first = new SpellgorgerBarbarian();
        Card second = new SpellgorgerBarbarian();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(first, second));

        barbarian.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Spellgorger Barbarian");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(barbarian.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
