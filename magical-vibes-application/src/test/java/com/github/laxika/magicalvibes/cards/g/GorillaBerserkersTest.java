package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.i.IvoryGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorillaBerserkers.class, ElvishRanger.class, IvoryGargoyle.class})
class GorillaBerserkersTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByOne() {
        addAttackingBerserkers();
        addBlockers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("Can't be blocked by fewer than three creatures")
    void cannotBeBlockedByTwo() {
        addAttackingBerserkers();
        addBlockers(3);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("With three blockers Rampage 2 grants +4/+4 until end of turn")
    void threeBlockersGivesPlusFour() {
        Permanent berserkers = addAttackingBerserkers();
        addBlockers(3);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        assertThat(berserkers.getPowerModifier()).isEqualTo(4);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("With four blockers Rampage 2 grants +6/+6 until end of turn")
    void fourBlockersGivesPlusSix() {
        Permanent berserkers = addAttackingBerserkers();
        addBlockers(4);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0)));
        harness.passBothPriorities();

        assertThat(berserkers.getPowerModifier()).isEqualTo(6);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    void rampageUsesBlockersAtResolution() {
        Permanent berserkers = addAttackingBerserkers();
        addCreatureReady(player2, new IvoryGargoyle());
        addBlockers(2);
        harness.addMana(player2, ManaColor.WHITE, 5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(berserkers.getPowerModifier()).isEqualTo(2);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void removingBlockerAfterRampageResolvesDoesNotChangeBonus() {
        Permanent berserkers = addAttackingBerserkers();
        Permanent gargoyle = addCreatureReady(player2, new IvoryGargoyle());
        addBlockers(2);
        harness.addMana(player2, ManaColor.WHITE, 5);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        resolveAllTriggers();

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gargoyle);
        assertThat(berserkers.getPowerModifier()).isEqualTo(4);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    void tramplesOverThreeBlockersWithRampageBonus() {
        addAttackingBerserkers();
        addBlockers(3);
        List<Permanent> blockers = List.copyOf(gd.playerBattlefields.get(player2.getId()));
        harness.setLife(player2, 20);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        resolveAllTriggers();
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blockers.get(0).getId(), 1,
                blockers.get(1).getId(), 1,
                blockers.get(2).getId(), 1,
                player2.getId(), 3));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContainAnyElementsOf(blockers);
        harness.assertInGraveyard(player1, "Gorilla Berserkers");
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent berserkers = addAttackingBerserkers();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(berserkers.getPowerModifier()).isZero();
    }

    @Test
    void mayRemainUnblockedWhenOnlyTwoBlockersAreAvailable() {
        Permanent berserkers = addAttackingBerserkers();
        addBlockers(2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(berserkers.getPowerModifier()).isZero();
        assertThat(berserkers.getToughnessModifier()).isZero();
    }

    @Test
    void rampageBonusExpiresAtEndOfTurn() {
        Permanent berserkers = addAttackingBerserkers();
        Permanent first = addCreatureReady(player2, new IvoryGargoyle());
        Permanent second = addCreatureReady(player2, new IvoryGargoyle());
        Permanent third = addCreatureReady(player2, new IvoryGargoyle());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        resolveAllTriggers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(4);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(4);
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                first.getId(), 2, second.getId(), 2, third.getId(), 2));
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(berserkers);
        assertThat(berserkers.getPowerModifier()).isZero();
        assertThat(berserkers.getToughnessModifier()).isZero();
    }

    private Permanent addAttackingBerserkers() {
        Permanent permanent = addCreatureReady(player1, new GorillaBerserkers());
        permanent.setAttacking(true);
        return permanent;
    }

    private void addBlockers(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new ElvishRanger());
        }
    }
}
