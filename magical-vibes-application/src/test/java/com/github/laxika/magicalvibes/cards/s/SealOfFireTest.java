package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealOfFire.class, MistralCharger.class, JaceBeleren.class, InvasionOfZendikar.class})
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

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Seal of Fire deals 2 damage to a target creature")
    void dealsDamageToCreature() {
        addSealOfFire();
        var target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());

        harness.activateAbility(player1, 0, null, target.getId());
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
        var target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Seal of Fire can target its controller")
    void dealsDamageToController() {
        harness.setLife(player1, 20);
        addSealOfFire();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Seal of Fire");
    }

    @Test
    @DisplayName("Seal of Fire can target its controller's creature")
    void dealsDamageToOwnCreature() {
        addSealOfFire();
        var target = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mistral Charger");
        harness.assertInGraveyard(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("Seal of Fire deals 2 damage to a target battle")
    void dealsDamageToBattle() {
        addSealOfFire();
        var battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
    }

    @Test
    @DisplayName("Seal of Fire rejects a noncreature enchantment before paying its cost")
    void rejectsEnchantmentTarget() {
        addSealOfFire();
        var target = harness.addToBattlefieldAndReturn(player2, new SealOfFire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seal of Fire");
        harness.assertOnBattlefield(player2, "Seal of Fire");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Seal of Fire can activate during the opponent's turn without mana")
    void activatesWhileTappedDuringOpponentsTurn() {
        harness.setLife(player2, 20);
        var seal = harness.addToBattlefieldAndReturn(player1, new SealOfFire());
        seal.tap();
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Seal of Fire");
    }

    private void addSealOfFire() {
        harness.addToBattlefield(player1, new SealOfFire());
    }
}
