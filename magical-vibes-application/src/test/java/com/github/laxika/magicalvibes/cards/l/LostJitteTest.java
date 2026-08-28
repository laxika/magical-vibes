package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({LostJitte.class, GrizzlyBears.class, Island.class})
class LostJitteTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature dealing combat damage puts a charge counter on Lost Jitte")
    void combatDamageAddsChargeCounter() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first mode untaps a target land and removes one charge counter")
    void untapLandMode() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent land = new Permanent(new Island());
        gd.playerBattlefields.get(player1.getId()).add(land);
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap mode cannot target a creature")
    void untapModeRequiresLandTarget() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second mode makes a target creature unable to block this turn")
    void cantBlockMode() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void cantBlockModeWearsOffAtEndOfTurn() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The third mode puts a +1/+1 counter on the equipped creature")
    void equippedCreatureCounterMode() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches Lost Jitte to a creature you control")
    void equipAttaches() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jitte.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addJitteReady(Player player) {
        Permanent jitte = new Permanent(new LostJitte());
        jitte.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jitte);
        return jitte;
    }
}
