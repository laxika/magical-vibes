package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheKingpinOfCrime.class, GiantSpider.class, GoblinPiker.class, GrizzlyBears.class})
class TheKingpinOfCrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying after attacking makes qualifying creatures assign toughness-based combat damage")
    void payingAfterAttackingUsesToughnessForQualifyingCreatures() {
        Permanent kingpin = addCreatureReady(player1, new TheKingpinOfCrime());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, kingpin)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the attack payment leaves combat damage unchanged")
    void decliningAttackPaymentDoesNothing() {
        addCreatureReady(player1, new TheKingpinOfCrime());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack payment effect expires at end of turn")
    void attackPaymentEffectExpiresAtEndOfTurn() {
        addCreatureReady(player1, new TheKingpinOfCrime());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Kingpin's extort drains an opponent when a spell is cast")
    void extortDrainsOpponent() {
        harness.addToBattlefield(player1, new TheKingpinOfCrime());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking with another creature triggers Kingpin while Kingpin stays home")
    void anotherCreatureAttackingTriggersKingpin() {
        harness.addToBattlefield(player1, new TheKingpinOfCrime());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("The paid effect applies to creatures entering later in the turn")
    void laterEnteringCreatureUsesToughness() {
        addCreatureReady(player1, new TheKingpinOfCrime());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent spider = harness.enterBattlefieldAndReturn(player1, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("The paid effect stops applying when a creature leaves your control")
    void damageEffectFollowsCurrentController() {
        addCreatureReady(player1, new TheKingpinOfCrime());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        gd.playerBattlefields.get(player1.getId()).remove(spider);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying affects neither opposing creatures nor power-based damage")
    void damageEffectOnlyChangesOwnCombatDamage() {
        Permanent kingpin = addCreatureReady(player1, new TheKingpinOfCrime());
        Permanent opposingSpider = addCreatureReady(player2, new GiantSpider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectiveCombatDamage(gd, opposingSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
        assertThat(gqs.getPowerBasedDamage(gd, kingpin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Having less than two life prevents the attack payment")
    void cannotPayWithInsufficientLife() {
        Permanent kingpin = addCreatureReady(player1, new TheKingpinOfCrime());
        harness.setLife(player1, 1);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 1);
        assertThat(gqs.getEffectiveCombatDamage(gd, kingpin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Extort accepts black mana and charges exactly one mana")
    void extortCanBePaidWithBlackMana() {
        harness.addToBattlefield(player1, new TheKingpinOfCrime());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining extort leaves both life totals unchanged")
    void decliningExtortDoesNotDrain() {
        harness.addToBattlefield(player1, new TheKingpinOfCrime());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Extort offers its payment only when the trigger resolves")
    void extortPaymentWaitsForResolution() {
        harness.addToBattlefield(player1, new TheKingpinOfCrime());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
