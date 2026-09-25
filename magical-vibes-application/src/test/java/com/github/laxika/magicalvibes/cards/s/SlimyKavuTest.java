package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlimyKavu.class, Forest.class})
class SlimyKavuTest extends BaseCardTest {

    @Test
    void targetLandBecomesSwampAndReplacesItsLandTypes() {
        Permanent forest = addKavuAndForest();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(findPermanent(player1, "Slimy Kavu").isTapped()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.SWAMP);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
        assertThat(gqs.getOverriddenLandManaColor(gd, forest)).isEqualTo(ManaColor.BLACK);
    }

    @Test
    void canTargetAnOpponentsLand() {
        addCreatureReady(player1, new SlimyKavu());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    void swampTypeWearsOffAtEndOfTurn() {
        Permanent forest = addKavuAndForest();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void cannotTargetANonLandPermanent() {
        addCreatureReady(player1, new SlimyKavu());
        Permanent otherKavu = addCreatureReady(player1, new SlimyKavu());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherKavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addKavuAndForest() {
        addCreatureReady(player1, new SlimyKavu());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        return forest;
    }
}
