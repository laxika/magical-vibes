package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MakindiGriffin;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
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

@CardUsed({Gloomhunter.class, GrizzlyBears.class, MakindiGriffin.class, SporecapSpider.class})
class GloomhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Gloomhunter can't be blocked by a creature without flying or reach")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new Gloomhunter());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gloomhunter can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new Gloomhunter());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MakindiGriffin());
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Gloomhunter can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent attacker = addCreatureReady(player1, new Gloomhunter());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SporecapSpider());
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Flying does not prevent Gloomhunter from blocking a ground creature")
    void canBlockGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new SporecapSpider());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Gloomhunter());
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }
}
