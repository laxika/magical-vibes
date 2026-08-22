package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DroverGrizzly.class, GrizzlyBears.class})
class DroverGrizzlyTest extends BaseCardTest {

    @Test
    @DisplayName("Saddled attack gives creatures you control trample until end of turn")
    void saddledAttackGrantsTrampleToCreaturesYouControl() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drover.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, drover, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drover, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isFalse();
        assertThat(drover.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Unsaddled attack does not grant trample")
    void unsaddledAttackDoesNotGrantTrample() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, drover, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isFalse();
    }
}
