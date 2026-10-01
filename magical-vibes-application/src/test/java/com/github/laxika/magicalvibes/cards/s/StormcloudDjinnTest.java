package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.CloudchaserKestrel;
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

@CardUsed({StormcloudDjinn.class, CloudchaserKestrel.class, BenalishCavalry.class})
class StormcloudDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Stormcloud Djinn can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent djinn = addCreatureReady(player2, new StormcloudDjinn());
        Permanent attacker = addCreatureReady(player1, new CloudchaserKestrel());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(djinn.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stormcloud Djinn cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new StormcloudDjinn());
        Permanent attacker = addCreatureReady(player1, new BenalishCavalry());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("The flying restriction applies only to Stormcloud Djinn")
    void restrictionAppliesOnlyToStormcloudDjinn() {
        Permanent djinn = addCreatureReady(player2, new StormcloudDjinn());
        Permanent unrestrictedBlocker = addCreatureReady(player2, new BenalishCavalry());
        Permanent attacker = addCreatureReady(player1, new BenalishCavalry());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(djinn.isBlocking()).isFalse();
        assertThat(unrestrictedBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Its ability gives it +2/+0 and deals 1 damage to its controller")
    void abilityBoostsAndDealsDamage() {
        Permanent djinn = addCreatureReady(player1, new StormcloudDjinn());
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(djinn.getPowerModifier()).isEqualTo(2);
        assertThat(djinn.getToughnessModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The ability's power boost wears off at end of turn")
    void abilityBoostWearsOffAtEndOfTurn() {
        Permanent djinn = addCreatureReady(player1, new StormcloudDjinn());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(djinn.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(djinn.getPowerModifier()).isZero();
    }
}
