package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyWingsEnfoldAll.class, DarkRitual.class, GrizzlyBears.class})
class MyWingsEnfoldAllTest extends BaseCardTest {

    @Test
    void drawsTwoCardsWhenThatModeIsChosen() {
        GrizzlyBears firstCard = new GrizzlyBears();
        GrizzlyBears secondCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        resolveScheme("Draw two cards");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
    }

    @Test
    void copiesControllerInstantSorceryUntilEndOfTurn() {
        resolveScheme("Until end of turn, whenever you cast an instant or sorcery spell, copy it. You may choose new targets for the copy");

        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);
        resolveRemainingStack();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(6);
    }

    private void resolveScheme(String mode) {
        Card sourceCard = new MyWingsEnfoldAll();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                player1.getId(),
                sourceCard.getName() + "'s set-in-motion ability",
                sourceCard.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    private void resolveRemainingStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
