package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RighteousIndignationTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a black creature gives the blocker +1/+1")
    void blackAttackerBoostsBlocker() {
        Permanent attacker = addReady(player1, new VampireAristocrat());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());
        addReady(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllRighteousIndignationTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking a red creature gives the blocker +1/+1")
    void redAttackerBoostsBlocker() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());
        addReady(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllRighteousIndignationTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking a nonblack, nonred creature does not boost the blocker")
    void otherColoredAttackerDoesNotBoostBlocker() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());
        addReady(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocker gets one boost for each black or red creature it blocks")
    void eachMatchingBlockedAttackerTriggersSeparately() {
        Permanent blackAttacker = addReady(player1, new VampireAristocrat());
        blackAttacker.setAttacking(true);
        Permanent redAttacker = addReady(player1, new HillGiant());
        redAttacker.setAttacking(true);
        Permanent blocker = addReady(player2, new WallOfGlare());
        addReady(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllRighteousIndignationTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    private void resolveAllRighteousIndignationTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
