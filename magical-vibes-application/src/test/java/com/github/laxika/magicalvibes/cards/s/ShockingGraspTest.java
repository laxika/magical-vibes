package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShockingGrasp.class, FountainOfYouth.class, GrizzlyBears.class})
class ShockingGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Shocking Grasp weakens a creature and draws a card")
    void weakensCreatureAndDrawsCard() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        FountainOfYouth drawnCard = new FountainOfYouth();
        harness.setHand(player1, List.of(new ShockingGrasp()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Shocking Grasp's power reduction expires at end of turn")
    void powerReductionExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShockingGrasp()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isZero();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Shocking Grasp cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShockingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Shocking Grasp does not draw when its target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        FountainOfYouth drawnCard = new FountainOfYouth();
        harness.setHand(player1, List.of(new ShockingGrasp()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Shocking Grasp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shocking Grasp can target your own creature and stack reductions below zero")
    void canTargetOwnCreatureAndReducePowerBelowZero() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        FountainOfYouth firstDraw = new FountainOfYouth();
        FountainOfYouth secondDraw = new FountainOfYouth();
        harness.setHand(player1, List.of(new ShockingGrasp(), new ShockingGrasp()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
