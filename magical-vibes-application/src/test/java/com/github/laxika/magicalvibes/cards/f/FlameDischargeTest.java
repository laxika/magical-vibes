package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjasKunai;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.cards.t.TamiyoCompleatedSage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameDischarge.class, AirElemental.class, GrizzlyBears.class,
        NinjasKunai.class, ShortCircuit.class, TamiyoCompleatedSage.class})
class FlameDischargeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage without a modified creature")
    void dealsXDamageWithoutModifiedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals X plus 2 damage when a modified creature was controlled as cast")
    void dealsXPlusTwoDamageWithModifiedCreature() {
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);

        gd.playerBattlefields.get(player1.getId()).remove(modifiedCreature);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void modificationGainedAfterCastingDoesNotGrantBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void opponentsModifiedCreatureDoesNotGrantBonus() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castFlameDischarge(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void zeroXDealsNoDamageWithoutModification() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target, 0);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void zeroXStillDealsTwoWithANonPowerToughnessCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target, 0);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void controlledAuraGrantsBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void opponentsAuraOnControlledCreatureDoesNotGrantBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void equipmentGrantsBonusEvenIfOpponentControlsEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new NinjasKunai());
        equipment.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsDamageToPlaneswalkerAndLoyaltyOnNoncreatureDoesNotGrantBonus() {
        Permanent ownPlaneswalker = harness.addToBattlefieldAndReturn(player1, new TamiyoCompleatedSage());
        ownPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TamiyoCompleatedSage());
        target.setCounterCount(CounterType.LOYALTY, 5);
        castFlameDischarge(target, 2);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void multipleModifiedCreaturesGrantOnlyOneBonusToPlaneswalkerDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TamiyoCompleatedSage());
        target.setCounterCount(CounterType.LOYALTY, 5);
        castFlameDischarge(target);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void canDealLethalDamageToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFlameDischarge(target, 2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FlameDischarge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFlameDischarge(Permanent target) {
        castFlameDischarge(target, 1);
    }

    private void castFlameDischarge(Permanent target, int x) {
        harness.setHand(player1, List.of(new FlameDischarge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);
        harness.castInstantForX(player1, 0, x, List.of(target.getId()));
    }
}
