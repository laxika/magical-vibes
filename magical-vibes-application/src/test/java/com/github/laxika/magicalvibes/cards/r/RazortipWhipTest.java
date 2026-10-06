package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BasilicaGuards;
import com.github.laxika.magicalvibes.cards.d.DomriRade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazortipWhip.class, BasilicaGuards.class, DomriRade.class})
class RazortipWhipTest extends BaseCardTest {

    @Test
    void damagesOpponentsPlaneswalkerWithoutDamagingItsController() {
        harness.addToBattlefield(player1, new RazortipWhip());
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriRade());
        domri.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, domri.getId());
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDamageOwnPlaneswalker() {
        harness.addToBattlefield(player1, new RazortipWhip());
        Permanent domri = harness.addToBattlefieldAndReturn(player1, new DomriRade());
        domri.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, domri.getId());
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    void abilityResolvesAfterWhipLeavesBattlefield() {
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new RazortipWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(whip);
        gd.playerGraveyards.get(player1.getId()).add(whip.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Ability deals 1 damage to target opponent")
    void deals1DamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RazortipWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Activating taps the artifact")
    void tapsAsCost() {
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new RazortipWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(whip.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself, only an opponent")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new RazortipWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target a creature, only opponent or planeswalker")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new RazortipWhip());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BasilicaGuards());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new RazortipWhip());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new RazortipWhip());
        whip.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
