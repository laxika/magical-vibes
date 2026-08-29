package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeatedDebateTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 4 damage to target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = new Permanent(new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        HeatedDebate heatedDebate = new HeatedDebate();
        harness.setHand(player1, List.of(heatedDebate));
        addHeatedDebateMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, heatedDebate.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    private void addHeatedDebateMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
