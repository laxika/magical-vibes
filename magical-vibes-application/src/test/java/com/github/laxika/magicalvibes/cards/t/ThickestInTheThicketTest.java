package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThickestInTheThicket.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class ThickestInTheThicketTest extends BaseCardTest {

    @Test
    void putsCountersEqualToTargetPowerOnEtb() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThickestInTheThicket()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void drawsTwoCardsWhenControllerHasGreatestPower() {
        harness.addToBattlefield(player1, new ThickestInTheThicket());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new HillGiant()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawsTwoCardsOnTieForGreatestPower() {
        harness.addToBattlefield(player1, new ThickestInTheThicket());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new HillGiant()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawWhenOpponentHasGreaterPower() {
        harness.addToBattlefield(player1, new ThickestInTheThicket());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ThickestInTheThicket()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, forest.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
