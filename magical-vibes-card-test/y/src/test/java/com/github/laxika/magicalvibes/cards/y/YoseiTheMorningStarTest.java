package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OrochiRanger;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.cards.g.GildedLight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoseiTheMorningStar.class, RendSpirit.class, Forest.class, OrochiRanger.class, RendFlesh.class, GildedLight.class})
class YoseiTheMorningStarTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger taps the chosen permanents and the target player skips their next untap step")
    void diesTapsChosenPermanentsAndSkipsUntapStep() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent bears = addCreatureReady(player2, new OrochiRanger());
        Permanent forest = addCreatureReady(player2, new Forest());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();

        endTurn(); // player 1 becomes active
        endTurn(); // player 2's turn — their untap step is skipped

        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only one untap step is skipped — the following one untaps normally")
    void skipsOnlyTheNextUntapStep() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent bears = addCreatureReady(player2, new OrochiRanger());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        endTurn();
        endTurn(); // player 2's turn — untap step skipped
        assertThat(bears.isTapped()).isTrue();

        endTurn();
        endTurn(); // player 2's next turn — untaps normally
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing no permanents is legal, but the untap step is still skipped")
    void choosingNoPermanentsStillSkipsUntapStep() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent bears = addCreatureReady(player2, new OrochiRanger());
        bears.tap();

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        endTurn();
        endTurn(); // player 2's turn — untap step still skipped

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The skip lands on the targeted player's untap-step queue and on no other queue")
    void queuesNothingButTheTargetPlayersUntapStepSkip() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        addCreatureReady(player2, new OrochiRanger());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.skipNextUntapStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
        assertThat(gd.skipNextTurnCount).isEmpty();
        assertThat(gd.skipNextDrawStepCount).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount).isEmpty();
    }

    @Test
    @DisplayName("Only the targeted player's permanents may be chosen, up to five of them")
    void onlyTargetPlayersPermanentsAreValidChoices() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent ownBears = addCreatureReady(player1, new OrochiRanger());
        Permanent enemyBears = addCreatureReady(player2, new OrochiRanger());
        Permanent enemyForest = addCreatureReady(player2, new Forest());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(enemyBears.getId(), enemyForest.getId())
                .doesNotContain(ownBears.getId());
    }

    @Test
    @DisplayName("The controller may target themselves")
    void mayTargetSelf() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent ownBears = addCreatureReady(player1, new OrochiRanger());

        killYosei();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, ownBears.getId());
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The permanent targets are chosen when the death trigger is put on the stack")
    void choosesPermanentTargetsWhenDeathTriggerIsPutOnStack() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent bears = addCreatureReady(player2, new OrochiRanger());
        Permanent forest = addCreatureReady(player2, new Forest());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice)
                .as("Yosei's five permanent targets must be selected before its trigger is put on the stack")
                .isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), forest.getId());
    }

    @Test
    @DisplayName("Five permanents may be targeted but a sixth stays untapped")
    void tapsAtMostFivePermanents() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        List<Permanent> forests = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new Forest()))
                .toList();

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        for (int i = 0; i < 5; i++) {
            harness.handlePermanentChosen(player1, forests.get(i).getId());
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(forests.subList(0, 5)).allMatch(Permanent::isTapped);
        assertThat(forests.get(5).isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap skip still applies when every permanent target has left the battlefield")
    void removedPermanentTargetsDoNotPreventUntapSkip() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent ranger = addCreatureReady(player2, new OrochiRanger());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, ranger.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        harness.setHand(player2, List.of(new RendFlesh()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, ranger.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ranger);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        endTurn();
        endTurn();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Separate deaths cause two consecutive untap steps to be skipped")
    void multipleDeathsSkipMultipleUntapSteps() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new YoseiTheMorningStar());
            killYosei();
            harness.handlePermanentChosen(player1, player2.getId());
            harness.handlePermanentChosen(player1, player1.getId());
            harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        }

        endTurn();
        endTurn();
        assertThat(forest.isTapped()).isTrue();
        endTurn();
        endTurn();
        assertThat(forest.isTapped()).isTrue();
        endTurn();
        endTurn();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A player who gains shroud in response is not affected by the untap skip")
    void illegalPlayerTargetDoesNotSkipUntap() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, forest.getId());

        harness.setHand(player2, List.of(new GildedLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.skipNextUntapStepCount.getOrDefault(player2.getId(), 0)).isZero();
    }

    private void killYosei() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        UUID yoseiId = harness.getPermanentId(player1, "Yosei, the Morning Star");
        harness.castAndResolveInstant(player2, 0, yoseiId);
    }

    /** Ends the current turn; the other player becomes active and takes their untap step. */
    private void endTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        var nextPlayer = gd.activePlayerId.equals(player1.getId()) ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(nextPlayer, TurnStep.UPKEEP);
    }
}
