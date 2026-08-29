package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvacynAngelOfHope;
import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulSearTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulSear()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        Card cardInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new SoulSear(), cardInHand)));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Target loses indestructible until end of turn")
    void losesIndestructibleUntilEndOfTurn() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player2, new AvacynAngelOfHope());
        harness.setHand(player1, List.of(new SoulSear()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThat(gqs.hasKeyword(gd, avacyn, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.castInstant(player1, 0, avacyn.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avacyn, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(avacyn.getMarkedDamage()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avacyn, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SoulSear()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature or planeswalker");
    }
}
