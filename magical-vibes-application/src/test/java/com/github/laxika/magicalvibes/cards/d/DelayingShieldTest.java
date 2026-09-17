package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DelayingShield.class, KamahlPitFighter.class, DuskImp.class})
class DelayingShieldTest extends BaseCardTest {

    private Permanent shield() {
        return harness.addToBattlefieldAndReturn(player1, new DelayingShield());
    }

    private void dealThreeDamageTo(Player target) {
        Permanent kamahl = addCreatureReady(player2, new KamahlPitFighter());
        int kamahlIndex = gd.playerBattlefields.get(player2.getId()).indexOf(kamahl);
        harness.activateAbility(player2, kamahlIndex, null, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Damage to the controller becomes delay counters")
    void replacesDamageWithDelayCounters() {
        Permanent shield = shield();
        int lifeBefore = gd.getLife(player1.getId());

        dealThreeDamageTo(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining each upkeep payment causes one life loss per delay counter")
    void decliningPaymentsLosesLifePerCounter() {
        Permanent shield = shield();
        dealThreeDamageTo(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
    }

    @Test
    @DisplayName("Paying {1}{W} for each counter avoids all upkeep life loss")
    void payingEachCounterAvoidsLifeLoss() {
        Permanent shield = shield();
        dealThreeDamageTo(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("The shield only replaces damage to its controller")
    void doesNotReplaceDamageToAnotherPlayer() {
        Permanent shield = shield();
        dealThreeDamageTo(player2);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
    }

    @Test
    @DisplayName("Combat damage to the controller becomes delay counters")
    void replacesCombatDamageWithDelayCounters() {
        Permanent shield = shield();
        int lifeBefore = gd.getLife(player1.getId());
        Permanent attacker = addCreatureReady(player2, new DuskImp());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isEqualTo(2);
    }

}
