package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ExposeTheCulprit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BubbleSmuggler.class, ExposeTheCulprit.class})
class BubbleSmugglerTest extends BaseCardTest {

    @Test
    void disguiseCastsBubbleSmugglerFaceDown() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Bubble Smuggler").isFaceDown()).isTrue();
    }

    @Test
    void putsFourCountersOnBubbleSmugglerWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent smuggler = findPermanent(player1, "Bubble Smuggler");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(smuggler));

        assertThat(smuggler.isFaceDown()).isFalse();
        assertThat(smuggler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFaceUpDoesNotPutCountersOnIt() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent smuggler = findPermanent(player1, "Bubble Smuggler");
        assertThat(smuggler.isFaceDown()).isFalse();
        assertThat(smuggler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTurnFaceUpForLessThanItsDisguiseCost() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent smuggler = findPermanent(player1, "Bubble Smuggler");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(smuggler)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(smuggler.isFaceDown()).isTrue();
        assertThat(smuggler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void freeTurnFaceUpAlsoPutsFourCountersOnIt() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent smuggler = findPermanent(player1, "Bubble Smuggler");
        harness.setHand(player1, List.of(new ExposeTheCulprit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(smuggler.getId()));
        resolveAllTriggers();

        assertThat(smuggler.isFaceDown()).isFalse();
        assertThat(smuggler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void disguiseWardCountersOpponentsSpellWhenTheyCannotPay() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent smuggler = findPermanent(player1, "Bubble Smuggler");
        harness.setHand(player2, List.of(new ExposeTheCulprit()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(smuggler.getId()));
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, false);
            resolveAllTriggers();
        }

        assertThat(smuggler.isFaceDown()).isTrue();
        assertThat(smuggler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
