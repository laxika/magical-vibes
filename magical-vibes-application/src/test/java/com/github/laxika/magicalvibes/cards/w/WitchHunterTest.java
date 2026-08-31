package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchHunter.class, Squire.class, DovinGrandArbiter.class})
class WitchHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent hunter = addCreatureReady(player1, new WitchHunter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to its controller")
    void dealsDamageToController() {
        harness.setLife(player1, 20);
        Permanent hunter = addCreatureReady(player1, new WitchHunter());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns target creature an opponent controls to its owner's hand")
    void returnsOpponentsCreatureToHand() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());
        addBounceMana(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Squire");
        harness.assertInHand(player2, "Squire");
    }

    @Test
    @DisplayName("Cannot activate the bounce ability without enough mana")
    void cannotActivateBounceAbilityWithoutEnoughMana() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player1, new Squire());
        addBounceMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a creature with the damage ability")
    void damageAbilityCannotTargetCreature() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a planeswalker with the bounce ability")
    void bounceAbilityCannotTargetPlaneswalker() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        target.setCounterCount(CounterType.LOYALTY, 3);
        addBounceMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    private void addBounceMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
