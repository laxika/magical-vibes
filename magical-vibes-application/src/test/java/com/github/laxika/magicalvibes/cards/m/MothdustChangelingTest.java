package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MothdustChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Taps another creature to gain flying until end of turn")
    void tapsAnotherCreatureToGainFlying() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);

        // Two untapped creatures -> choose which to tap
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(changeling.isTapped()).isFalse();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can tap itself as the only untapped creature to gain flying")
    void tapsItselfToGainFlying() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(changeling.isTapped()).isTrue();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());
        changeling.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
