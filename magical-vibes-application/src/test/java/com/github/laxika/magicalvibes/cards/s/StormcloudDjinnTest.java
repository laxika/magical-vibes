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

@CardUsed({StormcloudDjinn.class, CloudchaserKestrel.class, BenalishCavalry.class, Snapback.class})
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

    @Test
    @DisplayName("Repeated activations each boost the Djinn and damage its controller")
    void repeatedActivationsAccumulate() {
        Permanent djinn = addCreatureReady(player1, new StormcloudDjinn());
        harness.addMana(player1, ManaColor.RED, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(djinn.getPowerModifier()).isZero();
        harness.assertLife(player1, lifeBefore);
        resolveAllTriggers();

        assertThat(djinn.getPowerModifier()).isEqualTo(4);
        assertThat(djinn.getToughnessModifier()).isZero();
        harness.assertLife(player1, lifeBefore - 2);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("The ability still deals damage when the Djinn is returned to hand in response")
    void damageStillOccursAfterSourceLeavesBattlefield() {
        Permanent djinn = addCreatureReady(player1, new StormcloudDjinn());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Snapback()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, djinn.getId());
        harness.assertInHand(player1, "Stormcloud Djinn");
        harness.assertNotOnBattlefield(player1, "Stormcloud Djinn");
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("The ability can be activated while the Djinn is tapped and summoning sick")
    void abilityDoesNotRequireTappingOrHaste() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new StormcloudDjinn());
        djinn.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(djinn.getPowerModifier()).isEqualTo(2);
        assertThat(djinn.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 1);
    }
}
