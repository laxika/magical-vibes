package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LazotepSliver.class, SpinedSliver.class, GrizzlyBears.class, Shock.class})
class LazotepSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Slivers you control gain afflict 2")
    void grantsAfflictToOwnSlivers() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player1, new SpinedSliver());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        declareBlockers(0, 1);

        harness.passBothPriorities(); // Resolve Spined Sliver's trigger.
        harness.passBothPriorities(); // Resolve Lazotep Sliver's afflict trigger.

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A nontoken Sliver dying amasses Slivers 2")
    void amassesSliversWhenOwnNontokenSliverDies() {
        addCreatureReady(player1, new LazotepSliver());
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, sliver.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Sliver Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.SLIVER, CardSubtype.ARMY);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Sliver dying does not amass")
    void doesNotAmassWhenNonSliverDies() {
        addCreatureReady(player1, new LazotepSliver());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sliver Army")).isEmpty();
    }

    @Test
    @DisplayName("Lazotep Sliver's own death also amasses Slivers 2")
    void selfDeathAmassesSlivers() {
        Permanent lazotep = addCreatureReady(player1, new LazotepSliver());
        lazotep.setMarkedDamage(4);

        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        Permanent army = findPermanent(player1, "Sliver Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void declareBlockers(int blockerIndex, int attackerIndex) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
