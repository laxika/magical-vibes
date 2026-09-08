package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferiWhoSlowsTheSunset.class, MindStone.class, GrizzlyBears.class, Island.class})
class TeferiWhoSlowsTheSunsetTest extends BaseCardTest {

    @Test
    @DisplayName("+1 untaps your targets, taps opposing targets, and gains 2 life")
    void plusOneChangesTapStatesByControllerAndGainsLife() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Permanent artifact = addReadyPermanent(player1, new MindStone());
        Permanent creature = addReadyPermanent(player2, new GrizzlyBears());
        Permanent land = addReadyPermanent(player1, new Island());
        artifact.tap();
        land.tap();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(artifact.getId(), creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 can be activated without choosing targets")
    void plusOneAllowsNoTargets() {
        Permanent teferi = addReadyTeferi(player1, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 puts one of the top three cards into hand and the rest on the bottom")
    void minusTwoChoosesOneCardAndBottomOrdersTheRest() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Card first = new GrizzlyBears();
        Card chosen = new Island();
        Card third = new MindStone();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, chosen, third));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice search =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.allCards()).containsExactly(first, chosen, third);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.toBottom()).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-7 creates an emblem that untaps on opponents' untap steps and draws on opponents' draw steps")
    void minusSevenCreatesOpponentStepEmblem() {
        Permanent teferi = addReadyTeferi(player1, 7);
        Permanent permanent = addReadyPermanent(player1, new GrizzlyBears());
        permanent.tap();
        Card drawnByEmblem = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnByEmblem));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        advanceToNextTurn(player1);
        assertThat(permanent.isTapped()).isFalse();

        advanceToDraw(player2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnByEmblem);
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TeferiWhoSlowsTheSunset());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
