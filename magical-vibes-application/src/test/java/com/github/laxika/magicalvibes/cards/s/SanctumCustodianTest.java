package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumCustodian.class, GorillaWarrior.class, HeatRay.class, Forest.class})
class SanctumCustodianTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target creature")
    void preventsNextTwoDamageToCreature() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent target = addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstantForX(player2, 0, 3, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents damage only to the chosen target")
    void preventsDamageOnlyToChosenTarget() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent protectedTarget = addCreatureReady(player2, new GorillaWarrior());
        Permanent otherTarget = addCreatureReady(player2, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, protectedTarget.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstantForX(player2, 0, 1, List.of(otherTarget.getId()));
        harness.passBothPriorities();

        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(otherTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target player")
    void preventsNextTwoDamageToPlayer() {
        addCreatureReady(player1, new SanctumCustodian());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The shield is consumed across separate damage events")
    void shieldIsConsumedAcrossDamageEvents() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent target = addCreatureReady(player2, new GorillaWarrior());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeatRay(), new HeatRay(), new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 6);
        for (int i = 0; i < 3; i++) {
            harness.castInstant(player2, 0, 1, target.getId());
            harness.passBothPriorities();
            assertThat(target.getMarkedDamage()).isEqualTo(i < 2 ? 0 : 1);
        }
    }

    @Test
    @DisplayName("Unused prevention expires at the end of the turn")
    void unusedPreventionExpires() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent target = addCreatureReady(player2, new GorillaWarrior());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two custodians provide four points of prevention to the same target")
    void multipleShieldsAccumulate() {
        addCreatureReady(player1, new SanctumCustodian());
        addCreatureReady(player1, new SanctumCustodian());
        Permanent target = addCreatureReady(player2, new GorillaWarrior());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castInstantForX(player2, 0, 5, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
    }

    @Test
    @DisplayName("The ability resolves even if the custodian dies in response")
    void abilitySurvivesSourceRemoval() {
        Permanent custodian = addCreatureReady(player1, new SanctumCustodian());
        Permanent target = addCreatureReady(player1, new GorillaWarrior());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player2, List.of(new HeatRay(), new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 7);
        harness.castInstantForX(player2, 0, 2, List.of(custodian.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sanctum Custodian");
        harness.passBothPriorities();

        harness.castInstantForX(player2, 0, 3, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activation taps the custodian and prevents a second activation")
    void activationPaysTapCost() {
        Permanent custodian = addCreatureReady(player1, new SanctumCustodian());
        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(custodian.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A summoning-sick custodian cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent custodian = addCreatureReady(player1, new SanctumCustodian());
        custodian.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(custodian.isTapped()).isFalse();
    }
}
