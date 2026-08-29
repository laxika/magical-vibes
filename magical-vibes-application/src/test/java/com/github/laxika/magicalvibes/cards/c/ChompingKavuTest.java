package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChompingKavu.class, GrizzlyBears.class, HillGiant.class})
class ChompingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and restricts its blockers")
    void backsUpAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castChompingKavu();
        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Backup allows another creature to be blocked by a creature with power 3 or greater")
    void backsUpAnotherCreatureAgainstHighPowerBlocker() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castChompingKavu();
        resolveEtbTargeting(target);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Backup targeting Chomping Kavu itself only puts on the counter")
    void backsUpItselfWithoutGrantingRestriction() {
        Permanent kavu = castChompingKavu();
        resolveEtbTargeting(kavu);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        kavu.setSummoningSick(false);
        kavu.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(kavu))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Backup's blocker restriction expires at the end of the turn")
    void grantedRestrictionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castChompingKavu();
        resolveEtbTargeting(target);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent castChompingKavu() {
        harness.setHand(player1, List.of(new ChompingKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ChompingKavu)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
