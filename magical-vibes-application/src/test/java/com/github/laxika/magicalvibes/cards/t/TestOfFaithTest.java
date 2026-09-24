package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.d.DrossGolem;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TestOfFaith.class, DarksteelGargoyle.class, DarksteelIngot.class, BarbedLightning.class,
        Fireball.class, DrossGolem.class})
class TestOfFaithTest extends BaseCardTest {

    @Test
    void preventsThreeDamageAndAddsThreePlusOneCounters() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.setHand(player2, List.of(new BarbedLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(gargoyle.getId()));
        harness.passBothPriorities();

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gargoyle.getMarkedDamage()).isZero();
    }

    @Test
    void preventsOnlyTheNextThreeDamage() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.setHand(player2, List.of(new BarbedLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(gargoyle.getId()));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 2, gargoyle.getId());

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gargoyle.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void preventsOnlyTheFirstThreeDamageOfALargerEvent() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 5, gargoyle.getId());

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gargoyle.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ingot.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.setHand(player2, List.of(new BarbedLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(gargoyle.getId()));
        harness.passBothPriorities();

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gargoyle.getMarkedDamage()).isZero();
    }

    @Test
    void preventsCombatDamageToTargetCreature() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new DrossGolem());
        attacker.setSummoningSick(false);
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(gargoyle),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat(player1);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gargoyle.getMarkedDamage()).isZero();
    }

    @Test
    void preventionShieldExpiresAtEndOfTurn() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new BarbedLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(gargoyle.getId()));
        harness.passBothPriorities();

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gargoyle.getMarkedDamage()).isEqualTo(3);
    }
}
