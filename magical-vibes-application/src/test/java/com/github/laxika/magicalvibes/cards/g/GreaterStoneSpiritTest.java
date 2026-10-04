package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SkySpirit;
import com.github.laxika.magicalvibes.cards.v.VulshokBerserker;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({GreaterStoneSpirit.class, SkySpirit.class, VulshokBerserker.class, Mountain.class})
class GreaterStoneSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by a creature with flying")
    void cannotBeBlockedByFlyingCreature() {
        Permanent blocker = addCreatureReady(player2, new SkySpirit());

        Permanent attacker = addCreatureReady(player1, new GreaterStoneSpirit());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature without flying")
    void canBeBlockedByNonFlyingCreature() {
        Permanent blocker = addCreatureReady(player2, new VulshokBerserker());

        Permanent attacker = addCreatureReady(player1, new GreaterStoneSpirit());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Target creature gets +0/+2 and the granted ability boosts it")
    void boostsAndGrantsFirebreathing() {
        addCreatureReady(player1, new GreaterStoneSpirit());
        Permanent berserker = addCreatureReady(player1, new VulshokBerserker());

        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, 0, null, berserker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new GreaterStoneSpirit());
        Permanent berserker = addCreatureReady(player2, new VulshokBerserker());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, 0, null, berserker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);

        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost and granted ability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new GreaterStoneSpirit());
        Permanent berserker = addCreatureReady(player1, new VulshokBerserker());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, 0, null, berserker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefieldAndReturn(player1, new GreaterStoneSpirit());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Can target itself while summoning sick and repeatedly activate the granted ability")
    void canTargetItselfAndRepeatedlyPump() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new GreaterStoneSpirit());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 0, null, spirit.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability resolves and its grant persists after the source leaves")
    void abilityWorksAfterSourceLeaves() {
        Permanent spirit = addCreatureReady(player1, new GreaterStoneSpirit());
        Permanent berserker = addCreatureReady(player1, new VulshokBerserker());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, berserker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(spirit);
        gd.playerGraveyards.get(player1.getId()).add(spirit.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple activations targeting the same creature stack their toughness boosts")
    void toughnessBoostsStack() {
        addCreatureReady(player1, new GreaterStoneSpirit());
        Permanent berserker = addCreatureReady(player1, new VulshokBerserker());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, 0, null, berserker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, berserker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(6);
    }
}
