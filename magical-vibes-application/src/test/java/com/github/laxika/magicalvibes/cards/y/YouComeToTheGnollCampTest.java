package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouComeToTheGnollCamp.class, DireWolfProwler.class, Island.class})
class YouComeToTheGnollCampTest extends BaseCardTest {

    private static final int INTIMIDATE_THEM = 0;
    private static final int FEND_THEM_OFF = 1;

    @Test
    void intimidateThemStopsUpToTwoCreaturesFromBlocking() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        cast(INTIMIDATE_THEM, List.of(first.getId(), second.getId()));

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isTrue();
        assertThat(third.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void intimidateThemCanChooseNoTargets() {
        cast(INTIMIDATE_THEM, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fendThemOffBoostsTargetCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        cast(FEND_THEM_OFF, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void bothModesRequireCreatureTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, INTIMIDATE_THEM, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, FEND_THEM_OFF, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void intimidateThemCanTargetOneCreatureAndExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        cast(INTIMIDATE_THEM, List.of(target.getId()));

        assertThat(target.isCantBlockThisTurn()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void intimidateThemRejectsThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, INTIMIDATE_THEM,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void intimidateThemStillAffectsRemainingTargetWhenOtherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        prepareSpell();
        harness.castModalInstant(player1, 0, INTIMIDATE_THEM, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, first));
        harness.passBothPriorities();

        assertThat(second.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fendThemOffCanBoostAnOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        cast(FEND_THEM_OFF, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void fendThemOffRequiresATarget() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FEND_THEM_OFF, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
    private void cast(int modeIndex, List<UUID> targetIds) {
        prepareSpell();
        harness.castModalInstant(player1, 0, modeIndex, targetIds);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouComeToTheGnollCamp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
