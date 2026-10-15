package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
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

@CardUsed({DefibrillatingCurrent.class, ChandraBoldPyromancer.class, BalothGorger.class})
class DefibrillatingCurrentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a creature and you gain 2 life")
    void damagesCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new BalothGorger());
        harness.setHand(player1, List.of(new DefibrillatingCurrent()));
        harness.setLife(player1, 20);
        addDefibrillatingCurrentMana();

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Baloth Gorger"));

        harness.assertInGraveyard(player2, "Baloth Gorger");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals 4 damage to a planeswalker and you gain 2 life")
    void damagesPlaneswalkerAndGainsLife() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        harness.setHand(player1, List.of(new DefibrillatingCurrent()));
        harness.setLife(player1, 20);
        addDefibrillatingCurrentMana();

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DefibrillatingCurrent()));
        addDefibrillatingCurrentMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No life is gained when the only target leaves the battlefield")
    void doesNotGainLifeWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        harness.setHand(player1, List.of(new DefibrillatingCurrent()));
        harness.setLife(player1, 20);
        addDefibrillatingCurrentMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInHand(player2, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Defibrillating Current");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gain 2 life even when all 4 damage is prevented")
    void gainsLifeWhenDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        target.setDamagePreventionShield(4);
        harness.setHand(player1, List.of(new DefibrillatingCurrent()));
        harness.setLife(player1, 20);
        addDefibrillatingCurrentMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Baloth Gorger");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals exactly 4 damage to a surviving creature")
    void marksFourDamageOnSurvivingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new DefibrillatingCurrent()));
        harness.setLife(player1, 20);
        addDefibrillatingCurrentMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Baloth Gorger");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Can target your own creature and still gain life")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new DefibrillatingCurrent()));
        harness.setLife(player1, 20);
        addDefibrillatingCurrentMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertLife(player1, 22);
    }

    private void addDefibrillatingCurrentMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
