package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarBehemoth.class, RedwoodTreefolk.class})
class LlanowarBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Taps another creature to give itself +1/+1")
    void tapsAnotherCreatureToBoostSelf() {
        Permanent behemoth = addCreatureReady(player1, new LlanowarBehemoth());
        Permanent treefolk = addCreatureReady(player1, new RedwoodTreefolk());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(behemoth);
        harness.activateAbility(player1, idx, null, null);

        // Two untapped creatures -> choose which to tap
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.isTapped()).isTrue();
        assertThat(behemoth.isTapped()).isFalse();
        assertThat(behemoth.getEffectivePower()).isEqualTo(5);
        assertThat(behemoth.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can tap itself as the only untapped creature to boost")
    void tapsItselfToBoost() {
        Permanent behemoth = addCreatureReady(player1, new LlanowarBehemoth());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(behemoth);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(behemoth.isTapped()).isTrue();
        assertThat(behemoth.getEffectivePower()).isEqualTo(5);
        assertThat(behemoth.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Activations stack and a later activation can tap itself")
    void activationsStackUntilEndOfTurn() {
        Permanent behemoth = addCreatureReady(player1, new LlanowarBehemoth());
        Permanent treefolk = addCreatureReady(player1, new RedwoodTreefolk());
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(behemoth);

        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, treefolk.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(treefolk.isTapped()).isTrue();
        assertThat(behemoth.isTapped()).isTrue();
        assertThat(behemoth.getEffectivePower()).isEqualTo(6);
        assertThat(behemoth.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent behemoth = addCreatureReady(player1, new LlanowarBehemoth());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(behemoth);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(behemoth.getEffectivePower()).isEqualTo(4);
        assertThat(behemoth.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        Permanent behemoth = addCreatureReady(player1, new LlanowarBehemoth());
        behemoth.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(behemoth);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's creature to pay the cost")
    void cannotTapOpponentsCreature() {
        Permanent behemoth = addCreatureReady(player1, new LlanowarBehemoth());
        behemoth.tap();
        Permanent opponentCreature = addCreatureReady(player2, new RedwoodTreefolk());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(behemoth);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.isTapped()).isFalse();
    }
}
