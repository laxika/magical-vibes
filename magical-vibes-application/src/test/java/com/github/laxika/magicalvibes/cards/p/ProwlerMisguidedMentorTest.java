package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProwlerMisguidedMentor.class, GrizzlyBears.class, HillGiant.class})
class ProwlerMisguidedMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent prowler = addAttackingProwler();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(prowler);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByHighPowerCreature() {
        Permanent prowler = addAttackingProwler();
        Permanent giant = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(prowler);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage puts a +1/+1 counter on another creature you control")
    void combatDamagePutsCounterOnAnotherCreature() {
        Permanent prowler = addAttackingProwler();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId()).doesNotContain(prowler.getId(), opposingBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addAttackingProwler() {
        Permanent prowler = addCreatureReady(player1, new ProwlerMisguidedMentor());
        prowler.setAttacking(true);
        return prowler;
    }
}
