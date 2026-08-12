package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Sets base toughness from the power of a creature it blocks")
    void setsBaseToughnessFromCreatureItBlocks() {
        Permanent sentinel = addReady(player1, new Sentinel());
        Permanent attacker = addReady(player2, new GrizzlyBears());
        attacker.setPowerModifier(2);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, sentinel), indexOf(player2, attacker))));

        harness.activateAbility(player1, indexOf(player1, sentinel), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(5);

        attacker.setPowerModifier(0);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(5);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sets base toughness from the power of a creature blocking it")
    void setsBaseToughnessFromCreatureBlockingIt() {
        Permanent sentinel = addReady(player1, new Sentinel());
        sentinel.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());
        blocker.setPowerModifier(1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, sentinel))));

        harness.activateAbility(player1, indexOf(player1, sentinel), null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking or blocked by Sentinel")
    void cannotTargetUnrelatedCreature() {
        Permanent sentinel = addReady(player1, new Sentinel());
        Permanent unrelated = addReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, sentinel), null, unrelated.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
