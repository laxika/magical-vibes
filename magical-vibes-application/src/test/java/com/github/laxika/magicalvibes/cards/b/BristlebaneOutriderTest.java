package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BristlebaneOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 after another creature enters under your control")
    void getsBoostAfterAnotherCreatureEnters() {
        harness.setHand(player1, List.of(new BristlebaneOutrider()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent outrider = findPermanent(player1, "Bristlebane Outrider");

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent outrider = addCreatureReady(player1, new BristlebaneOutrider());
        outrider.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(outrider);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent outrider = addCreatureReady(player1, new BristlebaneOutrider());
        outrider.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(outrider);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
