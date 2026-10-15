package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CauterySliver.class, JaceBeleren.class, SerraSphinx.class, SinewSliver.class})
class CauterySliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver can sacrifice itself to deal 1 damage to a player")
    void sacrificesToDealDamage() {
        Permanent sliver = addCreatureReady(player1, new CauterySliver());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sliver);
    }

    @Test
    @DisplayName("The damage ability can target a creature")
    void damageAbilityCanTargetCreature() {
        addCreatureReady(player1, new CauterySliver());
        Permanent target = addCreatureReady(player2, new SerraSphinx());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Sliver can sacrifice itself to prevent the next damage to a player")
    void sacrificesToPreventDamage() {
        addCreatureReady(player1, new CauterySliver());
        addCreatureReady(player1, new CauterySliver());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The prevention shield stops only the next damage")
    void preventionShieldStopsOnlyNextDamage() {
        addCreatureReady(player1, new CauterySliver());
        addCreatureReady(player1, new CauterySliver());
        addCreatureReady(player1, new CauterySliver());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The prevention ability can target a Sliver creature")
    void preventionAbilityCanTargetSliverCreature() {
        addCreatureReady(player1, new CauterySliver());
        Permanent target = addCreatureReady(player2, new SinewSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The prevention ability can target a planeswalker")
    void preventionAbilityCanTargetPlaneswalker() {
        addCreatureReady(player1, new CauterySliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Sliver can use the granted damage ability")
    void opponentSliverCanUseGrantedDamageAbility() {
        addCreatureReady(player1, new CauterySliver());
        Permanent sliver = addCreatureReady(player2, new SinewSliver());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sliver);
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void preventionShieldExpiresAtEndOfTurn() {
        addCreatureReady(player1, new CauterySliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The prevention ability cannot target a non-Sliver creature")
    void preventionAbilityCannotTargetNonSliverCreature() {
        addCreatureReady(player1, new CauterySliver());
        Permanent sphinx = addCreatureReady(player2, new SerraSphinx());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sliver creature");
    }

    @Test
    void opponentSliverCanPreventDamageToItsController() {
        addCreatureReady(player1, new CauterySliver());
        Permanent sliver = addCreatureReady(player2, new SinewSliver());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sliver);

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void damageAbilityCanTargetPlaneswalker() {
        addCreatureReady(player1, new CauterySliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void preventionShieldPreventsDamageToSliverCreature() {
        addCreatureReady(player1, new CauterySliver());
        addCreatureReady(player1, new CauterySliver());
        Permanent target = addCreatureReady(player2, new SinewSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void tappedSummoningSickSliverCanActivateDamageAbility() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new CauterySliver());
        sliver.setSummoningSick(true);
        sliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sliver);
    }
}
