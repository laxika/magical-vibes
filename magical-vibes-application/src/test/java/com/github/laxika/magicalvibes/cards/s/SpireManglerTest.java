package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpireManglerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a flying creature you control +2/+0 until end of turn")
    void etbBoostsTargetFlyingCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castSpireMangler();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, groundCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingFlyer)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can target Spire Mangler itself")
    void etbCanTargetItself() {
        Permanent mangler = castSpireMangler();

        harness.handlePermanentChosen(player1, mangler.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mangler)).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB cannot target a nonflying or opposing creature")
    void etbRequiresFlyingCreatureYouControl() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castSpireMangler();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, groundCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingFlyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        castSpireMangler();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    private Permanent castSpireMangler() {
        harness.setHand(player1, List.of(new SpireMangler()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Spire Mangler");
    }
}
