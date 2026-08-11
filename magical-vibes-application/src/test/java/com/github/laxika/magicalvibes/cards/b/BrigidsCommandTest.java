package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrigidsCommandTest extends BaseCardTest {

    @Test
    void copyKithkinAndCreateTokenForTargetPlayer() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(kithkin.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement()
                .extracting(permanent -> permanent.getCard().isToken())
                .isEqualTo(true);
    }

    @Test
    void boostAndFightModesResolveInCardTextOrder() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(creature.getId(), creature.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void copyModeRejectsKithkinControlledByOpponent() {
        Permanent opponentKithkin = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new BrigidsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(opponentKithkin.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
