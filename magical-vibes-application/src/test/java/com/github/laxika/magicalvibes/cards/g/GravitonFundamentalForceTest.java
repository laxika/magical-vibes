package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravitonFundamentalForce.class, Forest.class, GrizzlyBears.class})
class GravitonFundamentalForceTest extends BaseCardTest {

    private static final String FLYING_MODE = "Target creature gains flying until end of turn";
    private static final String TAP_MODE = "Tap target creature";

    @Test
    @DisplayName("The second card draw can give a target creature flying until end of turn")
    void givesTargetCreatureFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        drawCard();
        harness.passPriority(player1);
        harness.handleListChoice(player1, FLYING_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The second card draw can tap a target creature")
    void tapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        drawCard();
        harness.passPriority(player1);
        harness.handleListChoice(player1, TAP_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The modal trigger only allows creature targets")
    void rejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        drawCard();
        harness.passPriority(player1);
        harness.handleListChoice(player1, TAP_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The mode and target are announced before opponents can respond")
    void announcesTargetBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        drawCard();
        harness.passPriority(player1);
        harness.handleListChoice(player1, TAP_MODE);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(target.getId());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the second draw triggers, not the first or third")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        assertThat(gd.stack).isEmpty();

        drawCard();
        assertThat(gd.stack).hasSize(1);

        drawCard();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger Graviton")
    void ignoresOpponentDraws() {
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Draws before Graviton enters count toward the second card each turn")
    void countsDrawBeforeEnteringBattlefield() {
        prepareLibrary();
        drawCard();
        harness.addToBattlefield(player1, new GravitonFundamentalForce());

        drawCard();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller's second draw also triggers during an opponent's turn")
    void triggersDuringOpponentTurn() {
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        drawCard();
        assertThat(gd.stack).isEmpty();
        drawCard();

        assertThat(gd.stack).hasSize(1);
    }

    private void prepareLibrary() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
