package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed({NimbleBladeKhenra.class, Shock.class, GrizzlyBears.class})
class NimbleBladeKhenraTest extends BaseCardTest {

    private Permanent addKhenra() {
        Permanent khenra = harness.addToBattlefieldAndReturn(player1, new NimbleBladeKhenra());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return khenra;
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void noncreatureSpellPumps() {
        Permanent khenra = addKhenra();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent khenra = addKhenra();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger prowess")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent khenra = addKhenra();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(3);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent khenra = addKhenra();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess resolves before the spell that triggered it")
    void boostResolvesBeforeSpell() {
        Permanent khenra = addKhenra();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Multiple noncreature spells give cumulative boosts")
    void multipleSpellsGiveCumulativeBoosts() {
        Permanent khenra = addKhenra();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, khenra)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, khenra)).isEqualTo(3);
    }
}
