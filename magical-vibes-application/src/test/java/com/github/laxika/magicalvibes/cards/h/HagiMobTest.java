package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HagiMob.class, LlanowarElves.class, TyvarKell.class})
class HagiMobTest extends BaseCardTest {

    @Test
    @DisplayName("Boast deals 1 damage to target player")
    void boastDealsDamageToTargetPlayer() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        harness.setLife(player2, 20);
        addBoastMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Boast deals 1 damage to target creature")
    void boastDealsDamageToTargetCreature() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        addCreatureReady(player2, new LlanowarElves());
        addBoastMana();

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Boast requires Hagi Mob to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new HagiMob());
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("A tapped Hagi Mob can boast after attacking")
    void tappedCreatureCanBoast() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        hagiMob.tap();
        addBoastMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(hagiMob.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boast can target its own controller")
    void boastCanTargetController() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        addBoastMana();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Boast can target Hagi Mob itself")
    void boastCanTargetItself() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        addBoastMana();

        harness.activateAbility(player1, 0, null, hagiMob.getId());
        harness.passBothPriorities();

        assertThat(hagiMob.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hagi Mob");
    }

    @Test
    @DisplayName("Boast removes one loyalty from a target planeswalker")
    void boastDealsDamageToPlaneswalker() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        Permanent tyvar = harness.addToBattlefieldAndReturn(player2, new TyvarKell());
        tyvar.setCounterCount(CounterType.LOYALTY, 3);
        addBoastMana();

        harness.activateAbility(player1, 0, null, tyvar.getId());
        harness.passBothPriorities();

        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The boast activation limit applies before the first activation resolves")
    void boastLimitAppliesWhileAbilityIsOnStack() {
        Permanent hagiMob = addCreatureReady(player1, new HagiMob());
        hagiMob.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Another Hagi Mob attacking does not enable this creature's boast")
    void anotherCreaturesAttackDoesNotEnableBoast() {
        addCreatureReady(player1, new HagiMob());
        Permanent otherMob = addCreatureReady(player1, new HagiMob());
        otherMob.setAttackedThisTurn(true);
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
