package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.f.FeralAbomination;
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

@CardUsed({SorcererOfTheFang.class, ChandraNovicePyromancer.class, FeralAbomination.class})
class SorcererOfTheFangTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to target opponent and taps the creature")
    void dealsDamageToOpponent() {
        harness.setLife(player2, 20);
        Permanent sorcerer = addReadySorcerer(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(sorcerer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability deals 2 damage to target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent sorcerer = addReadySorcerer(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);
        addActivationMana();

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(sorcerer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addReadySorcerer(player1);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadySorcerer(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralAbomination());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadySorcerer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDamageOwnPlaneswalker() {
        addReadySorcerer(player1);
        Permanent planeswalker = addPlaneswalker(player1, 5);
        addActivationMana();

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new SorcererOfTheFang());
        sorcerer.setSummoningSick(true);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sorcerer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent sorcerer = addReadySorcerer(player1);
        sorcerer.setTapped(true);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayBlackCostWithOnlyColorlessMana() {
        addReadySorcerer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent sorcerer = addReadySorcerer(player1);
        addActivationMana();
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sorcerer);
        gd.playerGraveyards.get(player1.getId()).add(sorcerer.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityDoesNotResolveWhenPlaneswalkerTargetLeavesBattlefield() {
        harness.setLife(player2, 20);
        addReadySorcerer(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);
        addActivationMana();
        harness.activateAbility(player1, 0, null, planeswalker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());

        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private Permanent addReadySorcerer(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SorcererOfTheFang());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraNovicePyromancer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
