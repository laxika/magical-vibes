package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SealOfFire.class, MistralCharger.class, JaceBeleren.class})
class SealOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Seal of Fire sacrifices it as a cost")
    void sacrificesAsCost() {
        addSealOfFire();

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Seal of Fire");
        harness.assertInGraveyard(player1, "Seal of Fire");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Seal of Fire deals 2 damage to a target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addSealOfFire();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Seal of Fire deals 2 damage to a target creature")
    void dealsDamageToCreature() {
        addSealOfFire();
        harness.addToBattlefield(player2, new MistralCharger());

        var targetId = harness.getPermanentId(player2, "Mistral Charger");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mistral Charger");
        harness.assertInGraveyard(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Seal of Fire deals 2 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        addSealOfFire();
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Seal of Fire's ability fizzles when its target is removed")
    void fizzlesIfTargetRemoved() {
        addSealOfFire();
        harness.addToBattlefield(player2, new MistralCharger());

        var targetId = harness.getPermanentId(player2, "Mistral Charger");
        harness.activateAbility(player1, 0, null, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void addSealOfFire() {
        harness.addToBattlefield(player1, new SealOfFire());
    }
}
