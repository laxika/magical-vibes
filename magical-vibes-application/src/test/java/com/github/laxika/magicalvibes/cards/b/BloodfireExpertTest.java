package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BloodfireExpert.class, GrizzlyBears.class, Shock.class})
class BloodfireExpertTest extends BaseCardTest {

    private Permanent addExpert() {
        Permanent expert = harness.addToBattlefieldAndReturn(player1, new BloodfireExpert());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return expert;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void noncreatureSpellPumps() {
        Permanent expert = addExpert();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, expert)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent expert = addExpert();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, expert)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger prowess")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent expert = addExpert();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, expert)).isEqualTo(1);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent expert = addExpert();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(4);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, expert)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess resolves before the triggering spell")
    void prowessResolvesBeforeSpell() {
        Permanent expert = addExpert();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(3);
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, expert)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 2);
    }

    @Test
    @DisplayName("Two noncreature spells each trigger prowess once and their boosts accumulate")
    void multipleSpellsAccumulateBoosts() {
        Permanent expert = addExpert();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, expert)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, expert)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, expert)).isEqualTo(1);
    }
}
