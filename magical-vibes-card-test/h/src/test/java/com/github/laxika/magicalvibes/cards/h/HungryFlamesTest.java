package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RidgescaleTusker;
import com.github.laxika.magicalvibes.cards.s.SubmergedBoneyard;
import com.github.laxika.magicalvibes.cards.t.TezzeretTheSchemer;
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

@CardUsed({HungryFlames.class, RidgescaleTusker.class, SubmergedBoneyard.class, TezzeretTheSchemer.class})
class HungryFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the creature and 2 damage to the player")
    void dealsDamageToBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());
        harness.setHand(player1, List.of(new HungryFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), player2.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Rejects a land as either target")
    void rejectsLandTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SubmergedBoneyard());
        harness.setHand(player1, List.of(new HungryFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesPlaneswalkerAndCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretTheSchemer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDamageOwnCreatureAndSelf() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RidgescaleTusker());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), player1.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 18);
    }

    @Test
    void stillDamagesPlayerWhenCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());
        prepareSpell();
        harness.castInstant(player1, 0, List.of(creature.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Hungry Flames");
    }

    @Test
    void stillDamagesCreatureWhenPlaneswalkerLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretTheSchemer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        prepareSpell();
        harness.castInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);

        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Ridgescale Tusker");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotResolveWhenBothTargetsLeave() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretTheSchemer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        prepareSpell();
        harness.castInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(creature, planeswalker));

        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Hungry Flames");
    }

    @Test
    void requiresBothTargetsAndRejectsWrongTargetTypes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new HungryFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
    }
}
