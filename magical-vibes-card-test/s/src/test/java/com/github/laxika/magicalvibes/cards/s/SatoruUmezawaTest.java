package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoonCircuitHacker;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
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

@CardUsed({SatoruUmezawa.class, GrizzlyBears.class, Shock.class, FangOfShigeki.class,
        MoonCircuitHacker.class, TamiyosCompleation.class})
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

    @Test
    void losingAbilitiesStopsGrantingNinjutsu() {
        Permanent attacker = addCreatureReady(player1, new FangOfShigeki());
        Permanent satoru = harness.addToBattlefieldAndReturn(player1, new SatoruUmezawa());
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, satoru.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new FangOfShigeki()));
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }

    @Test
    void losingAbilitiesStopsNinjutsuTrigger() {
        Permanent attacker = addCreatureReady(player1, new FangOfShigeki());
        Permanent satoru = harness.addToBattlefieldAndReturn(player1, new SatoruUmezawa());
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, satoru.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new MoonCircuitHacker()));
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MoonCircuitHacker.class);
    }

    @Test
    void returningSatoruAsCostDoesNotTriggerButGrantedNinjutsuStillResolves() {
        Permanent satoru = addCreatureReady(player1, new SatoruUmezawa());
        Card ninja = new FangOfShigeki();
        harness.setHand(player1, List.of(ninja));
        satoru.setAttacking(true);
        satoru.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, satoru.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(satoru.getCard(), ninja);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        Permanent entered = findPermanent(player1, "Fang of Shigeki");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ninja);
    }

    @Test
    void nativeNinjutsuTriggerWithOneCardLibraryPutsThatCardInHand() {
        Permanent attacker = addCreatureReady(player1, new FangOfShigeki());
        harness.addToBattlefield(player1, new SatoruUmezawa());
        Card ninja = new MoonCircuitHacker();
        Card onlyCard = new FangOfShigeki();
        harness.setHand(player1, List.of(ninja));
        harness.setLibrary(player1, List.of(onlyCard));
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).contains(ninja, onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
