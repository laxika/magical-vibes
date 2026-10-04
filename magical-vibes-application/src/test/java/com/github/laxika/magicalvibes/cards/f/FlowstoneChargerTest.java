package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowstoneCharger.class})
class FlowstoneChargerTest extends BaseCardTest {

    @Test
    void attackingGivesItPlusThreeMinusThreeUntilEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new FlowstoneCharger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(3);
        assertThat(charger.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    void onlyTheAttackingChargerGetsTheBoost() {
        Permanent attackingCharger = addCreatureReady(player1, new FlowstoneCharger());
        Permanent nonAttackingCharger = addCreatureReady(player1, new FlowstoneCharger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attackingCharger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attackingCharger)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonAttackingCharger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttackingCharger)).isEqualTo(5);
    }

    @Test
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new FlowstoneCharger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(charger.getPowerModifier()).isZero();
        assertThat(charger.getToughnessModifier()).isZero();
    }

    @Test
    void boostWaitsForTheAttackTriggerToResolve() {
        Permanent charger = addCreatureReady(player1, new FlowstoneCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));

            assertThat(gd.stack).hasSize(1);
            assertThat(charger.getPowerModifier()).isZero();
            assertThat(charger.getToughnessModifier()).isZero();

            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(2);
        });
    }

    @Test
    void blockingDoesNotGiveTheBoostAndBothChargersDealLethalDamage() {
        Permanent attacker = addCreatureReady(player1, new FlowstoneCharger());
        Permanent blocker = addCreatureReady(player2, new FlowstoneCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Flowstone Charger");
        harness.assertInGraveyard(player2, "Flowstone Charger");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
