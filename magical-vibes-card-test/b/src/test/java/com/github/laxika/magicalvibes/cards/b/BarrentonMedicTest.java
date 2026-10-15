package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WiltLeafCavaliers;
import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BarrentonMedic.class, WiltLeafCavaliers.class, MeliraSylvokOutcast.class})
class BarrentonMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability prevents the next 1 damage to a targeted player")
    void tapAbilityPreventsDamage() {
        addReadyMedic(player1);
        Permanent attacker = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat(player1);

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Tap ability prevents the next 1 damage to a targeted creature")
    void tapAbilityPreventsDamageToCreature() {
        addReadyMedic(player1);
        Permanent attacker = addCreatureReady(player1, new WiltLeafCavaliers());
        Permanent target = addReadyMedic(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(player1, List.of(attackerIndex));
        int targetIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(targetIndex, attackerIndex)));
        resolveCombat(player1);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Untap ability untaps the Medic")
    void untapAbilityUntapsMedic() {
        Permanent medic = addReadyMedic(player1);
        medic.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(medic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability puts a -1/-1 counter on the Medic as a cost (paid on activation)")
    void untapAbilityPutsMinusCounterOnActivation() {
        Permanent medic = addReadyMedic(player1);
        medic.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        // Cost is paid immediately on activation, before the ability resolves.
        assertThat(medic.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each untap activation stacks another -1/-1 counter")
    void untapAbilityStacksCounters() {
        Permanent medic = addReadyMedic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(medic.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untap ability can be activated while summoning sick")
    void untapAbilityWorksWhileSummoningSick() {
        Permanent medic = harness.addToBattlefieldAndReturn(player1, new BarrentonMedic());
        medic.setSummoningSick(true);
        medic.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(medic.isTapped()).isTrue();
        assertThat(medic.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(medic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void tapAbilityCannotBeActivatedWhileSummoningSick() {
        Permanent medic = harness.addToBattlefieldAndReturn(player1, new BarrentonMedic());
        medic.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(medic.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A fourth counter kills the Medic before its untap ability resolves")
    void lethalCounterCostRemovesMedicBeforeResolution() {
        Permanent medic = addReadyMedic(player1);
        medic.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        medic.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Barrenton Medic");
        harness.assertInGraveyard(player1, "Barrenton Medic");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Barrenton Medic");
    }

    @Test
    @DisplayName("Untapping and activating again stacks prevention on the same player")
    void repeatedTapActivationsPreventTwoDamage() {
        Permanent medic = addReadyMedic(player1);
        Permanent attacker = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(medic.isTapped()).isTrue();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat(player1);

        harness.assertLife(player2, 19);
        assertThat(medic.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({BarrentonMedic.class, MeliraSylvokOutcast.class})
    @DisplayName("Melira prevents paying the untap ability's counter cost")
    void cannotActivateUntapAbilityWhenMinusCountersAreForbidden() {
        Permanent medic = addReadyMedic(player1);
        medic.tap();
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(medic.isTapped()).isTrue();
        assertThat(medic.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMedic(Player player) {
        return addCreatureReady(player, new BarrentonMedic());
    }
}
