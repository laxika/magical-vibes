package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndRazeForerunners.class, SauroformHybrid.class})
class EndRazeForerunnersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives other creatures you control +2/+2, vigilance, and trample")
    void etbBoostsOtherOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        castEndRazeForerunners();

        Permanent endRaze = findPermanent(player1, "End-Raze Forerunners");
        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(endRaze.getEffectivePower()).isEqualTo(7);
        assertThat(endRaze.getEffectiveToughness()).isEqualTo(7);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and keyword grants wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        castEndRazeForerunners();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive its effects")
    void creaturesAreChosenAtResolution() {
        harness.castFromHand(player1, new EndRazeForerunners(), "{5}{G}{G}{G}");
        harness.passBothPriorities();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        resolveAllTriggers();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the effects")
    void laterCreaturesAreNotAffected() {
        castEndRazeForerunners();

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());
        resolveAllTriggers();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The trigger resolves even if End-Raze Forerunners leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.castFromHand(player1, new EndRazeForerunners(), "{5}{G}{G}{G}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "End-Raze Forerunners");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, source));

        resolveAllTriggers();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Separate triggers stack and boost another End-Raze Forerunners")
    void multipleTriggersStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        castEndRazeForerunners();
        Permanent first = findPermanent(player1, "End-Raze Forerunners");
        castEndRazeForerunners();
        Permanent second = findPermanents(player1, "End-Raze Forerunners").get(1);

        assertThat(creature.getEffectivePower()).isEqualTo(6);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);
        assertThat(first.getEffectivePower()).isEqualTo(9);
        assertThat(first.getEffectiveToughness()).isEqualTo(9);
        assertThat(second.getEffectivePower()).isEqualTo(7);
        assertThat(second.getEffectiveToughness()).isEqualTo(7);
    }

    private void castEndRazeForerunners() {
        harness.castFromHand(player1, new EndRazeForerunners(), "{5}{G}{G}{G}");
        resolveAllTriggers();
    }
}
