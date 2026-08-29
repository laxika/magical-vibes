package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
