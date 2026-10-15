package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedpodSquire.class, GrizzlyBears.class, WindDrake.class})
class SeedpodSquireTest extends BaseCardTest {

    @Test
    void attackTriggerOnlyTargetsControlledCreaturesWithoutFlying() {
        Permanent squire = addCreatureReady(player1, new SeedpodSquire());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId())
                .doesNotContain(squire.getId(), drake.getId(), opponentBears.getId());
    }

    @Test
    void attackTriggerGivesChosenCreaturePlusOnePlusOneUntilEndOfTurn() {
        addCreatureReady(player1, new SeedpodSquire());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void boostExpiresAtCleanup() {
        addCreatureReady(player1, new SeedpodSquire());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    void targetThatGainsFlyingBeforeResolutionIsNotBoosted() {
        addCreatureReady(player1, new SeedpodSquire());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        bears.getGrantedKeywords().add(Keyword.FLYING);
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetThatChangesControllerBeforeResolutionIsNotBoosted() {
        addCreatureReady(player1, new SeedpodSquire());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillResolvesAfterSquireLeavesBattlefield() {
        Permanent squire = addCreatureReady(player1, new SeedpodSquire());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(squire);
        gd.playerGraveyards.get(player1.getId()).add(squire.getCard());
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void attackWithNoLegalTargetDoesNotLeaveAPendingChoice() {
        addCreatureReady(player1, new SeedpodSquire());
        addCreatureReady(player1, new WindDrake());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(opponentBears.getPowerModifier()).isZero();
        assertThat(opponentBears.getToughnessModifier()).isZero();
    }
}
