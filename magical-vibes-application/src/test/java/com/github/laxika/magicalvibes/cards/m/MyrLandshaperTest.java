package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlinkmothNexus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrLandshaper.class, BlinkmothNexus.class})
class MyrLandshaperTest extends BaseCardTest {

    @Test
    void makesTargetLandAnArtifactUntilEndOfTurn() {
        Permanent source = addCreatureReady(player1, new MyrLandshaper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlinkmothNexus());

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isLand(gd, target)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.isLand(gd, target)).isTrue();
    }

    @Test
    void onlyTargetsLands() {
        Permanent source = addCreatureReady(player1, new MyrLandshaper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MyrLandshaper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
        assertThat(source.isTapped()).isFalse();
    }
}
