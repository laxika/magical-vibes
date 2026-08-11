package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonStyleTwinsTest extends BaseCardTest {

    private Permanent addTwins() {
        harness.addToBattlefield(player1, new DragonStyleTwins());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
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

        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(4);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(3);
    }
}
