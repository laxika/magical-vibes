package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.IceFloe;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Updraft.class, BalduvianBears.class, IceFloe.class})
class UpdraftTest extends BaseCardTest {

    @Test
    @DisplayName("Grants target creature flying and schedules a draw at the next upkeep")
    void grantsFlyingAndSchedulesDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Updraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        GameData gd = harness.getGameData();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new IceFloe());
        harness.setHand(player1, List.of(new Updraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setLibrary(player1, List.of(new BalduvianBears()));
        harness.setHand(player1, List.of(new Updraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        GameData gd = harness.getGameData();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Does not schedule the draw if the target leaves before resolution")
    void doesNotScheduleDrawIfTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Updraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        harness.assertInGraveyard(player1, "Updraft");
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Updraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        GameData gd = harness.getGameData();

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Drawing is delayed, uses the stack, and survives the opposing target leaving")
    void delayedDrawSurvivesTargetLeavingAfterResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setLibrary(player1, List.of(new BalduvianBears(), new BalduvianBears()));
        harness.setLibrary(player2, List.of(new BalduvianBears(), new BalduvianBears()));
        harness.setHand(player1, List.of(new Updraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A draw registered during upkeep waits for a new turn, including an extra turn")
    void drawWaitsForNextTurnWithSameActivePlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setLibrary(player1, List.of(new BalduvianBears()));
        harness.setHand(player1, List.of(new Updraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
