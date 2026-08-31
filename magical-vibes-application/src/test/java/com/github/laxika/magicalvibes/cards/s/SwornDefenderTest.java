package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwornDefender.class, ElvishRanger.class})
class SwornDefenderTest extends BaseCardTest {

    @Test
    @DisplayName("Power becomes blocker's toughness minus 1 and toughness becomes blocker's power plus 1")
    void copiesStatsFromBlocker() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent blocker = addRanger(player2, 2, 5);

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Blocker is 2/5 -> Sworn Defender becomes 4/3
        assertThat(defender.getEffectivePower()).isEqualTo(4);
        assertThat(defender.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Works against a creature it is blocking")
    void copiesStatsFromAttackerItBlocks() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent attacker = addRanger(player2, 4, 2);

        setupDefenderBlockingAttacker(defender, attacker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        // Attacker is 4/2 -> Sworn Defender becomes 1/5
        assertThat(defender.getEffectivePower()).isEqualTo(1);
        assertThat(defender.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Uses the target's current, boosted power and toughness")
    void usesBoostedStatsOfTarget() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent blocker = addRanger(player2, 2, 2);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // 4/4

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(defender.getEffectivePower()).isEqualTo(3);
        assertThat(defender.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Reads the target's power and toughness when the ability resolves")
    void readsStatsAtResolution() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent blocker = addRanger(player2, 2, 2);

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setPowerModifier(2);
        blocker.setToughnessModifier(3);
        harness.passBothPriorities();

        assertThat(defender.getEffectivePower()).isEqualTo(4);
        assertThat(defender.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Values are locked in at resolution — later changes to the target do not update them")
    void locksInValuesAtResolution() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent blocker = addRanger(player2, 2, 2);

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(defender.getEffectivePower()).isEqualTo(1);
        assertThat(defender.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature not blocking or blocked by it")
    void cannotTargetCreatureNotInCombat() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent unrelated = addRanger(player2, 2, 2);

        declareAttackers(player1, List.of(indexOf(player1, defender)));
        prepareDeclareBlockers(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, unrelated.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing when the target is no longer in combat at resolution")
    void targetMustStillBeInCombatAtResolution() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent blocker = addRanger(player2, 2, 2);

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        blocker.getBlockingTargetIds().clear();
        harness.passBothPriorities();

        assertThat(defender.getEffectivePower()).isEqualTo(1);
        assertThat(defender.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Effect wears off at cleanup")
    void wearsOffAtCleanup() {
        Permanent defender = addCreatureReady(player1, new SwornDefender());
        Permanent blocker = addRanger(player2, 2, 5);

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(defender.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(defender.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(defender.getEffectivePower()).isEqualTo(1);
        assertThat(defender.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addRanger(Player player, int power, int toughness) {
        ElvishRanger ranger = new ElvishRanger();
        ranger.setPower(power);
        ranger.setToughness(toughness);
        return addCreatureReady(player, ranger);
    }

    private void setupDefenderAttackingBlockedBy(Permanent defender, Permanent blocker) {
        declareAttackers(player1, List.of(indexOf(player1, defender)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, defender))));
    }

    private void setupDefenderBlockingAttacker(Permanent defender, Permanent attacker) {
        declareAttackers(player2, List.of(indexOf(player2, attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, defender), indexOf(player2, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
