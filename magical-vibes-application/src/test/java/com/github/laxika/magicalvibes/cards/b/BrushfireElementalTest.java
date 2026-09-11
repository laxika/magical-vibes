package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrushfireElemental.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class BrushfireElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Brushfire Elemental +2/+2 until end of turn")
    void landfallBoostsBrushfireElemental() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new BrushfireElemental());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new BrushfireElemental());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(1);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new BrushfireElemental());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(1);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Brushfire Elemental cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent elemental = addCreatureReady(player1, new BrushfireElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(elemental)));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elemental)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Brushfire Elemental can be blocked by a creature with power greater than 2")
    void canBeBlockedByHighPowerCreature() {
        Permanent elemental = addCreatureReady(player1, new BrushfireElemental());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(elemental)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elemental))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
