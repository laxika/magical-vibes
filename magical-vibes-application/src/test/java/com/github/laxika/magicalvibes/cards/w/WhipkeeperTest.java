package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StoneTongueBasilisk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whipkeeper.class, StoneTongueBasilisk.class, DwarvenGrunt.class, Firebolt.class, Forest.class})
class WhipkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the damage dealt to the target this turn")
    void dealsDamageEqualToDamageAlreadyDealtThisTurn() {
        Permanent whipkeeper = addCreatureReady(player1, new Whipkeeper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneTongueBasilisk());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isEqualTo(2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(whipkeeper.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetCreatureControlledByItsController() {
        addCreatureReady(player1, new Whipkeeper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneTongueBasilisk());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Counts damage removed by regeneration")
    void countsDamageRemovedByRegeneration() {
        addCreatureReady(player1, new Whipkeeper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count prevented damage")
    void doesNotCountPreventedDamage() {
        addCreatureReady(player1, new Whipkeeper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        target.setDamagePreventionShield(2);
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new Whipkeeper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
