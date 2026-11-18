package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RetreatToValakut.class, Forest.class, GrizzlyBears.class})
class RetreatToValakutTest extends BaseCardTest {

    private static final String BOOST_MODE = "Target creature gets +2/+0 until end of turn.";
    private static final String CANT_BLOCK_MODE = "Target creature can't block this turn.";

    @Test
    void landfallBoostsTargetCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player1, new RetreatToValakut());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, BOOST_MODE);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void landfallMakesTargetCreatureUnableToBlockThisTurn() {
        harness.addToBattlefield(player1, new RetreatToValakut());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, CANT_BLOCK_MODE);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.hasFloatingEffectOnPermanent(bears.getId(), CantBlockEffect.class)).isTrue();
    }

    @Test
    void modesCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new RetreatToValakut());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, BOOST_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostModeCanTargetOwnCreatureAndDoesNotApplyBeforeResolution() {
        harness.addToBattlefield(player1, new RetreatToValakut());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, BOOST_MODE);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gd.hasFloatingEffectOnPermanent(bears.getId(), CantBlockEffect.class)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.hasFloatingEffectOnPermanent(bears.getId(), CantBlockEffect.class)).isFalse();
    }

    @Test
    void cantBlockModeAffectsOnlyChosenCreatureAndExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new RetreatToValakut());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, CANT_BLOCK_MODE);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.hasFloatingEffectOnPermanent(chosen.getId(), CantBlockEffect.class)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.hasFloatingEffectOnPermanent(chosen.getId(), CantBlockEffect.class)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(2);
        assertThat(gd.hasFloatingEffectOnPermanent(other.getId(), CantBlockEffect.class)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.hasFloatingEffectOnPermanent(chosen.getId(), CantBlockEffect.class)).isFalse();
        assertThat(gd.hasFloatingEffectOnPermanent(other.getId(), CantBlockEffect.class)).isFalse();
    }

    @Test
    void cantBlockModeCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new RetreatToValakut());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, CANT_BLOCK_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new RetreatToValakut());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gd.hasFloatingEffectOnPermanent(bears.getId(), CantBlockEffect.class)).isFalse();
    }
}
