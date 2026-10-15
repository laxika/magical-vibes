package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JackOLantern;
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

@CardUsed({TeferiWhoSlowsTheSunset.class, JackOLantern.class, CandlegroveWitch.class, Island.class})
class TeferiWhoSlowsTheSunsetTest extends BaseCardTest {

    @Test
    @DisplayName("+1 untaps your targets, taps opposing targets, and gains 2 life")
    void plusOneChangesTapStatesByControllerAndGainsLife() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Permanent artifact = addCreatureReady(player1, new JackOLantern());
        Permanent creature = addCreatureReady(player2, new CandlegroveWitch());
        Permanent land = addCreatureReady(player1, new Island());
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
        Card first = new CandlegroveWitch();
        Card chosen = new Island();
        Card third = new JackOLantern();
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
        Permanent permanent = addCreatureReady(player1, new CandlegroveWitch());
        permanent.tap();
        Card drawnByEmblem = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnByEmblem));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        harness.performUntapStep(player2);
        assertThat(permanent.isTapped()).isFalse();

        advanceToDraw(player2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnByEmblem);
    }

    @Test
    @DisplayName("+1 can choose only a creature, omitting the artifact and land")
    void plusOneAllowsOnlyCreatureTarget() {
        addReadyTeferi(player1, 4);
        Permanent creature = addCreatureReady(player2, new CandlegroveWitch());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("+1 can choose only a land, omitting the artifact and creature")
    void plusOneAllowsOnlyLandTarget() {
        addReadyTeferi(player1, 4);
        Permanent land = addCreatureReady(player1, new Island());
        land.tap();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("+1 does not gain life when its only target has left the battlefield")
    void plusOneDoesNotGainLifeWhenAllTargetsAreIllegal() {
        addReadyTeferi(player1, 4);
        Permanent artifact = addCreatureReady(player1, new JackOLantern());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(artifact.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        harness.setGraveyard(player1, List.of(artifact.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("+1 resolves for surviving targets and gains life when one target leaves")
    void plusOneResolvesWithOneIllegalTarget() {
        addReadyTeferi(player1, 4);
        Permanent artifact = addCreatureReady(player1, new JackOLantern());
        Permanent creature = addCreatureReady(player2, new CandlegroveWitch());
        Permanent land = addCreatureReady(player1, new Island());
        land.tap();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(artifact.getId(), creature.getId(), land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        harness.setGraveyard(player1, List.of(artifact.getCard()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("-2 puts the only card in a short library into hand")
    void minusTwoWithOneCardInLibrary() {
        addReadyTeferi(player1, 4);
        Card card = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(card));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("-2 with an empty library does not attempt to draw")
    void minusTwoWithEmptyLibrary() {
        addReadyTeferi(player1, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("-2 preserves the unseen library and allows reversing the bottom cards")
    void minusTwoBottomsCardsAfterUnseenLibraryInChosenOrder() {
        addReadyTeferi(player1, 4);
        Card first = new CandlegroveWitch();
        Card chosen = new Island();
        Card third = new JackOLantern();
        Card unseen = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, chosen, third, unseen));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, third, first);
    }

    @Test
    @DisplayName("The emblem does not draw an extra card during its controller's draw step")
    void emblemDoesNotDrawOnControllersDrawStep() {
        addReadyTeferi(player1, 7);
        Card normalDraw = new Island();
        Card nextCard = new JackOLantern();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(normalDraw, nextCard));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(normalDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TeferiWhoSlowsTheSunset());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
