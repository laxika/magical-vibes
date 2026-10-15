package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BurnFromWithin;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.h.HulkingDevil;
import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SilentObserver;
import com.github.laxika.magicalvibes.cards.s.SorinGrimNemesis;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({WolfOfDevilsBreach.class, DevilthornFox.class, HulkingDevil.class,
        SilentObserver.class, Mountain.class, BurnFromWithin.class, SorinGrimNemesis.class, JustTheWind.class})
class WolfOfDevilsBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can pay and discard to deal damage equal to the discarded card's mana value")
    void attackingPaysAndDiscardsForManaValueDamage() {
        addReadyWolf();
        Permanent target = addCreatureReady(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new HulkingDevil()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Devilthorn Fox");
        harness.assertInGraveyard(player1, "Hulking Devil");
    }

    @Test
    @DisplayName("Declining the attack trigger does not discard")
    void decliningDoesNothing() {
        addReadyWolf();
        HulkingDevil discarded = new HulkingDevil();
        harness.setHand(player1, List.of(discarded));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, findPermanent(player1, "Wolf of Devil's Breach").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger does not pay or discard when its mana cost cannot be paid")
    void cannotPayDoesNothing() {
        addReadyWolf();
        HulkingDevil discarded = new HulkingDevil();
        harness.setHand(player1, List.of(discarded));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, findPermanent(player1, "Wolf of Devil's Breach").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Discard damage happens immediately during resolution and equals the card's mana value")
    void damageResolvesWithoutAnotherPriorityWindow() {
        addReadyWolf();
        Permanent target = addCreatureReady(player2, new SilentObserver());
        resolvePaidAttack(target, new HulkingDevil());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Silent Observer");
        harness.assertInGraveyard(player1, "Hulking Devil");
    }

    @Test
    @DisplayName("Discarding a land pays the discard cost but deals no damage")
    void landDiscardDealsZeroDamage() {
        addReadyWolf();
        Permanent target = addCreatureReady(player2, new SilentObserver());
        resolvePaidAttack(target, new Mountain());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("An X in the discarded card's mana cost counts as zero")
    void xInDiscardedManaCostCountsAsZero() {
        addReadyWolf();
        Permanent target = addCreatureReady(player2, new SilentObserver());
        resolvePaidAttack(target, new BurnFromWithin());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Burn from Within");
    }

    @Test
    @DisplayName("The attack trigger can damage a planeswalker")
    void canTargetPlaneswalker() {
        addReadyWolf();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SorinGrimNemesis());
        target.setCounterCount(CounterType.LOYALTY, 6);
        resolvePaidAttack(target, new HulkingDevil());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Sorin, Grim Nemesis");
    }

    @Test
    @DisplayName("Mana cannot be spent on the combined cost with no card to discard")
    void emptyHandCannotPayCombinedCost() {
        Permanent wolf = addReadyWolf();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, wolf.getId());
            harness.passBothPriorities();
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, true);
            }

            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
            assertThat(wolf.getMarkedDamage()).isZero();
            assertThat(gd.interaction.activeInteraction()).isNull();
        });
    }

    @Test
    @DisplayName("An illegal target prevents resolution without paying mana or discarding")
    void targetLeavingBeforeResolutionPreventsPayment() {
        addReadyWolf();
        Permanent target = addCreatureReady(player2, new SilentObserver());
        HulkingDevil discarded = new HulkingDevil();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passPriority(player1);
            harness.castAndResolveInstant(player2, 0, target.getId());
            harness.passBothPriorities();

            harness.assertInHand(player2, "Silent Observer");
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
        });
    }

    private void resolvePaidAttack(Permanent target, Card discarded) {
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
        });
    }

    private Permanent addReadyWolf() {
        return addCreatureReady(player1, new WolfOfDevilsBreach());
    }
}
