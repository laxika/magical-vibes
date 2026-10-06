package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TundraWolves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sentinel.class, TundraWolves.class})
class SentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Sets base toughness from the power of a creature it blocks")
    void setsBaseToughnessFromCreatureItBlocks() {
        Permanent sentinel = addCreatureReady(player1, new Sentinel());
        Permanent attacker = addCreatureReady(player2, new TundraWolves());
        attacker.setPowerModifier(2);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, sentinel), indexOf(player2, attacker))));

        harness.activateAbility(player1, indexOf(player1, sentinel), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);

        attacker.setPowerModifier(0);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sets base toughness from the power of a creature blocking it")
    void setsBaseToughnessFromCreatureBlockingIt() {
        Permanent sentinel = addCreatureReady(player1, new Sentinel());
        sentinel.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TundraWolves());
        blocker.setPowerModifier(1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, sentinel))));

        harness.activateAbility(player1, indexOf(player1, sentinel), null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking or blocked by Sentinel")
    void cannotTargetUnrelatedCreature() {
        Permanent sentinel = addCreatureReady(player1, new Sentinel());
        Permanent unrelated = addCreatureReady(player2, new TundraWolves());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, sentinel), null, unrelated.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses target power at resolution and preserves toughness modifiers")
    void usesPowerAtResolutionWithToughnessModifiers() {
        Permanent sentinel = addCreatureReady(player1, new Sentinel());
        sentinel.setToughnessModifier(2);
        Permanent attacker = addCreatureReady(player2, new Sentinel());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, sentinel), indexOf(player2, attacker))));

        harness.activateAbility(player1, indexOf(player1, sentinel), null, attacker.getId());
        attacker.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
    }

    @Test
    @DisplayName("A later activation replaces the previous base toughness")
    void laterActivationReplacesPreviousBaseToughness() {
        Permanent sentinel = addCreatureReady(player1, new Sentinel());
        Permanent attacker = addCreatureReady(player2, new Sentinel());
        attacker.setAttacking(true);
        attacker.setPowerModifier(3);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, sentinel), indexOf(player2, attacker))));

        harness.activateAbility(player1, indexOf(player1, sentinel), null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(5);

        attacker.setPowerModifier(0);
        harness.activateAbility(player1, indexOf(player1, sentinel), null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not resolve when the target is no longer in combat with Sentinel")
    void doesNotResolveWhenCombatRelationshipEnds() {
        Permanent sentinel = addCreatureReady(player1, new Sentinel());
        Permanent attacker = addCreatureReady(player2, new Sentinel());
        attacker.setAttacking(true);
        attacker.setPowerModifier(3);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, sentinel), indexOf(player2, attacker))));

        harness.activateAbility(player1, indexOf(player1, sentinel), null, attacker.getId());
        sentinel.setBlocking(false);
        sentinel.getBlockingTargetIds().clear();
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
