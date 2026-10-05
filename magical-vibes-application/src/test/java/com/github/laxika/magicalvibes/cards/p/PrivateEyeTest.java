package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrivateEye.class, NoviceInspector.class, SanitationAutomaton.class})
class PrivateEyeTest extends BaseCardTest {

    @Test
    void boostsOtherDetectivesYouControl() {
        Permanent detective = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        Permanent nonDetective = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());

        int detectivePower = gqs.getEffectivePower(gd, detective);
        int detectiveToughness = gqs.getEffectiveToughness(gd, detective);
        int nonDetectivePower = gqs.getEffectivePower(gd, nonDetective);
        int nonDetectiveToughness = gqs.getEffectiveToughness(gd, nonDetective);
        harness.addToBattlefield(player1, new PrivateEye());

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(detectivePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(detectiveToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonDetective)).isEqualTo(nonDetectivePower);
        assertThat(gqs.getEffectiveToughness(gd, nonDetective)).isEqualTo(nonDetectiveToughness);
    }

    @Test
    void makesAChosenDetectiveUnblockableAfterSecondDraw() {
        harness.addToBattlefield(player1, new PrivateEye());
        Permanent opponentDetective = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        Permanent opponentNonDetective = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SanitationAutomaton(), new SanitationAutomaton()));

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(opponentDetective.getId())
                .doesNotContain(opponentNonDetective.getId());

        harness.handlePermanentChosen(player1, opponentDetective.getId());
        harness.passBothPriorities();

        assertThat(opponentDetective.isCantBeBlocked()).isTrue();
    }

    @Test
    void excludesItselfAndOpponentsButMultipleEyesBoostEachOther() {
        Permanent firstEye = harness.addToBattlefieldAndReturn(player1, new PrivateEye());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        assertThat(gqs.getEffectivePower(gd, firstEye)).isEqualTo(firstEye.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, firstEye)).isEqualTo(firstEye.getCard().getToughness());
        int firstPower = gqs.getEffectivePower(gd, firstEye);
        int firstToughness = gqs.getEffectiveToughness(gd, firstEye);
        int opponentPower = gqs.getEffectivePower(gd, opponent);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponent);

        Permanent secondEye = harness.addToBattlefieldAndReturn(player1, new PrivateEye());

        assertThat(gqs.getEffectivePower(gd, firstEye)).isEqualTo(firstPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, firstEye)).isEqualTo(firstToughness + 1);
        assertThat(gqs.getEffectivePower(gd, secondEye)).isEqualTo(firstPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, secondEye)).isEqualTo(firstToughness + 1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(opponentToughness);

        gd.playerBattlefields.get(player1.getId()).remove(secondEye);

        assertThat(gqs.getEffectivePower(gd, firstEye)).isEqualTo(firstPower);
        assertThat(gqs.getEffectiveToughness(gd, firstEye)).isEqualTo(firstToughness);
    }

    @Test
    void triggersOnlyOnControllersSecondDrawEvenDuringOpponentsTurnAndExpires() {
        gd.activePlayerId = player2.getId();
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new PrivateEye());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SanitationAutomaton(), new SanitationAutomaton(),
                new SanitationAutomaton(), new SanitationAutomaton(), new SanitationAutomaton(), new SanitationAutomaton()));
        harness.setLibrary(player2, List.of(new SanitationAutomaton(), new SanitationAutomaton()));

        drawCard(player2);
        drawCard(player2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(eye.getId());
        harness.handlePermanentChosen(player1, eye.getId());
        assertThat(eye.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        assertThat(eye.isCantBeBlocked()).isTrue();

        drawCard(player1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(eye.isCantBeBlocked()).isFalse();
        drawCard(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(eye.getId());
        harness.handlePermanentChosen(player1, eye.getId());
        harness.passBothPriorities();
        assertThat(eye.isCantBeBlocked()).isTrue();
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
