package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.u.UnassumingSage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonshakerCavalry.class, UnassumingSage.class})
class MoonshakerCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives your creatures flying and a boost equal to your creature count")
    void etbCountsCreaturesYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent secondOwnCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());

        castMoonshakerCavalry();

        Permanent moonshaker = findPermanent(player1, "Moonshaker Cavalry");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondOwnCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondOwnCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, moonshaker)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, moonshaker)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondOwnCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The temporary boost and flying grant wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());

        castMoonshakerCavalry();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The creature count is determined when the trigger resolves")
    void countsCreaturesAtResolution() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent moonshaker = harness.enterBattlefieldAndReturn(player1, new MoonshakerCavalry());
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, moonshaker)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, moonshaker)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The boost stays fixed and creatures entering after resolution are unaffected")
    void laterCreaturesDoNotChangeResolvedEffect() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        castMoonshakerCavalry();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The trigger resolves after Cavalry leaves and counts only remaining creatures")
    void triggerResolvesWithoutSource() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent moonshaker = harness.enterBattlefieldAndReturn(player1, new MoonshakerCavalry());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, moonshaker));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Moonshaker Cavalry");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
    }

    private void castMoonshakerCavalry() {
        harness.castFromHand(player1, new MoonshakerCavalry(), "{5}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
