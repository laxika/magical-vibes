package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaraudingMako;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeronicaDissidentScribe.class, Forest.class, GrizzlyBears.class, MaraudingMako.class})
class VeronicaDissidentScribeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking may discard to draw and create a Junk")
    void attacksMayDiscardDrawAndCreateJunk() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addCreatureReady(player1, new VeronicaDissidentScribe());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding a land does not create a Junk")
    void landDiscardDoesNotCreateJunk() {
        Card discarded = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addCreatureReady(player1, new VeronicaDissidentScribe());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    @DisplayName("The nonland discard trigger fires only once each turn")
    void nonlandDiscardTriggerFiresOnlyOnceEachTurn() {
        Card firstDiscard = new GrizzlyBears();
        Card secondDiscard = new MaraudingMako();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new VeronicaDissidentScribe());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
    }

    @Test
    @DisplayName("Declining the attack discard leaves the hand unchanged")
    void decliningAttackDiscardDoesNothing() {
        Card card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        addCreatureReady(player1, new VeronicaDissidentScribe());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    @DisplayName("A nonland discarded before Veronica enters already uses the first discard of the turn")
    void earlierNonlandDiscardPreventsJunk() {
        Card firstDiscard = new MaraudingMako();
        Card secondDiscard = new MaraudingMako();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        addCreatureReady(player1, new VeronicaDissidentScribe());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    @DisplayName("Discarding a land first does not consume the nonland discard trigger")
    void landBeforeNonlandStillCreatesJunk() {
        harness.setHand(player1, List.of(new Forest(), new MaraudingMako()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new VeronicaDissidentScribe());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Junk")).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with an empty hand cannot draw a card")
    void emptyHandDoesNotDraw() {
        Card topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new VeronicaDissidentScribe());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    @DisplayName("Junk sacrifices itself to exile a card that can be cast by paying its cost")
    void junkExilesAndAllowsCasting() {
        Card exiled = new MaraudingMako();
        harness.setHand(player1, List.of(new VeronicaDissidentScribe()));
        harness.setLibrary(player1, List.of(new Forest(), exiled));
        addCreatureReady(player1, new VeronicaDissidentScribe());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, exiled.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Marauding Mako");
    }

}
