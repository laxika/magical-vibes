package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonStyleTwins.class, GrizzlyBears.class, Shock.class})
class DragonStyleTwinsTest extends BaseCardTest {

    private Permanent addTwins() {
        Permanent twins = harness.addToBattlefieldAndReturn(player1, new DragonStyleTwins());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return twins;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void noncreatureSpellPumps() {
        Permanent twins = addTwins();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent twins = addTwins();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger prowess")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent twins = addTwins();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(3);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent twins = addTwins();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(4);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess resolves before the spell that triggered it")
    void prowessResolvesBeforeSpell() {
        Permanent twins = addTwins();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each noncreature spell adds a separate prowess boost")
    void multipleSpellsGiveCumulativeBoosts() {
        Permanent twins = addTwins();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(5);

        endTurn();
        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(3);
    }

    @Test
    @DisplayName("Double strike deals damage in both combat damage steps")
    void unblockedTwinsDealDamageTwice() {
        addCreatureReady(player1, new DragonStyleTwins());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Prowess increases damage in both double-strike damage steps")
    void prowessBoostAppliesToBothCombatDamageSteps() {
        Permanent twins = addTwins();
        twins.setSummoningSick(false);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 10);
    }
}
