package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LockjawSlobberingTeleporter.class, BurstOfStrength.class, GrizzlyBears.class})
class LockjawSlobberingTeleporterTest extends BaseCardTest {

    @Test
    void castsNoncreatureSpellThenGainsCounterAndMakesTwoCreaturesUnblockable() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, otherCreature.getId());
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(otherCreature.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player1.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(lockjaw.getId(), opposingCreature.getId());

        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lockjaw.isCantBeBlocked()).isTrue();
        assertThat(otherCreature.isCantBeBlocked()).isTrue();
        assertThat(opposingCreature.isCantBeBlocked()).isFalse();
    }

    @Test
    void doesNotTriggerWithoutCastingANoncreatureSpell() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());

        advanceToBeginningOfCombat();

        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lockjaw.isCantBeBlocked()).isFalse();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
