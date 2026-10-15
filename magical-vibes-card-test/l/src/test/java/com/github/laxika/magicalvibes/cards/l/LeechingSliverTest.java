package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BelligerentSliver;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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

@CardUsed({LeechingSliver.class, BelligerentSliver.class, RuneclawBear.class, TurnToFrog.class})
class LeechingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player loses 1 life when a Sliver attacks")
    void triggersWhenSliverAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new LeechingSliver());
        addCreatureReady(player1, new BelligerentSliver());

        declareAttackers(List.of(1)); // 2/2 Sliver attacks

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leeching Sliver");

        harness.passBothPriorities();

        // 20 - 1 (trigger) - 2 (combat damage) = 17
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Leeching Sliver is itself a Sliver — triggers when it attacks")
    void triggersWhenItselfAttacks() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new LeechingSliver());

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // 20 - 1 (trigger) - 1 (1/1 combat damage) = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Triggers once per attacking Sliver")
    void triggersPerSliver() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new LeechingSliver());
        addCreatureReady(player1, new BelligerentSliver());
        addCreatureReady(player1, new BelligerentSliver());

        declareAttackers(List.of(1, 2));

        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // 20 - 2 (triggers) - 4 (two 2/2s) = 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not trigger when a non-Sliver attacks")
    void doesNotTriggerForNonSliver() {
        addCreatureReady(player1, new LeechingSliver());
        addCreatureReady(player1, new RuneclawBear());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's attacking Sliver")
    void doesNotTriggerForOpponentSliver() {
        addCreatureReady(player1, new LeechingSliver());
        addCreatureReady(player2, new BelligerentSliver());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Leeching Sliver"))
                .count()).isZero();
    }

    @Test
    @DisplayName("Each Leeching Sliver triggers independently for the same attacker")
    void multipleLeechingSliversTriggerIndependently() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LeechingSliver());
        addCreatureReady(player1, new LeechingSliver());
        addCreatureReady(player1, new BelligerentSliver());

        declareAttackers(List.of(2));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Leeching Sliver cannot trigger after losing all abilities")
    void doesNotTriggerAfterLosingAbilities() {
        Permanent source = addCreatureReady(player1, new LeechingSliver());
        addCreatureReady(player1, new BelligerentSliver());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Sliver turned into a Frog does not trigger Leeching Sliver")
    void doesNotTriggerForCreatureThatLostSliverType() {
        addCreatureReady(player1, new LeechingSliver());
        Permanent attacker = addCreatureReady(player1, new BelligerentSliver());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.stack).isEmpty();
    }
}
