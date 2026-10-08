package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DefiantKhenra;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZealotOfTheGodPharaoh.class, DefiantKhenra.class, NicolBolasGodPharaoh.class})
class ZealotOfTheGodPharaohTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target opponent")
    void deals2DamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ability does not tap the creature")
    void doesNotTap() {
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new ZealotOfTheGodPharaoh());
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(zealot.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target a creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DefiantKhenra());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);
        addActivationMana();

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 2);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDamageOwnPlaneswalker() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player1, new NicolBolasGodPharaoh());
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);
        addActivationMana();

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 2);
        harness.assertLife(player1, 20);
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new ZealotOfTheGodPharaoh());
        zealot.tap();
        zealot.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(zealot.isTapped()).isTrue();
    }

    @Test
    void cannotPayActivationWithOnlyColorlessMana() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new ZealotOfTheGodPharaoh());
        addActivationMana();
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(zealot);
        gd.playerGraveyards.get(player1.getId()).add(zealot.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotDamagePlayerWhenTargetPlaneswalkerLeavesBattlefield() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        addActivationMana();
        harness.activateAbility(player1, 0, null, planeswalker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
