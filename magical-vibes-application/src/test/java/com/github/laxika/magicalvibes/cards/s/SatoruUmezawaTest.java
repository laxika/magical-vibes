package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SatoruUmezawa.class, GrizzlyBears.class, Shock.class})
class SatoruUmezawaTest extends BaseCardTest {

    @Test
    @DisplayName("Grants ninjutsu to creature cards in its controller's hand")
    void grantsNinjutsuOnlyToControllerCreatureCards() {
        harness.addToBattlefield(player1, new SatoruUmezawa());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }

    @Test
    @DisplayName("Activating granted ninjutsu looks at three cards, keeps one, and orders the rest on bottom")
    void ninjutsuTriggerLooksAtTopThree() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SatoruUmezawa());
        Card ninja = new GrizzlyBears();
        Card first = new Shock();
        Card second = new GrizzlyBears();
        Card third = new Shock();
        harness.setHand(player1, List.of(ninja));
        harness.setLibrary(player1, List.of(first, second, third));
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first);
    }

    @Test
    @DisplayName("The ninjutsu trigger fires only once each turn")
    void ninjutsuTriggerFiresOnceEachTurn() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SatoruUmezawa());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        declareAttackers(List.of(0, 1));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, firstAttacker.getId());
        harness.activateHandAbility(player1, 0, secondAttacker.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Satoru Umezawa")))
                .hasSize(1);
    }
}
