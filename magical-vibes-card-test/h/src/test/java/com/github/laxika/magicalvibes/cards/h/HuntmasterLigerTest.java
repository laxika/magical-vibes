package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntmasterLiger.class, GrizzlyBears.class})
class HuntmasterLigerTest extends BaseCardTest {

    @Test
    @DisplayName("Mutations boost other creatures by the number of times Huntmaster Liger mutated")
    void mutationsScaleTheBoostAndExcludeTheSource() {
        Permanent liger = addCreatureReady(player1, new HuntmasterLiger());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, liger, List.of(liger.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, liger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, liger)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, liger, List.of(liger.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(5);
    }
    @Test
    @CardUsed(HuntmasterLiger.class)
    void pendingTriggersUseTheMutationCountAtResolution() {
        Permanent source = addCreatureReady(player1, new HuntmasterLiger());
        Permanent ally = addCreatureReady(player1, new HuntmasterLiger());

        triggerMutation(source);
        triggerMutation(source);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(4);
    }

    @Test
    @CardUsed(HuntmasterLiger.class)
    void onlyCreaturesPresentAtResolutionAreBoostedUntilEndOfTurn() {
        Permanent source = addCreatureReady(player1, new HuntmasterLiger());
        triggerMutation(source);
        Permanent ally = addCreatureReady(player1, new HuntmasterLiger());
        resolveAllTriggers();
        Permanent lateAlly = addCreatureReady(player1, new HuntmasterLiger());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, lateAlly)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lateAlly)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(4);
    }

    @Test
    @CardUsed(HuntmasterLiger.class)
    void normalCreatureCastingDoesNotTriggerTheBoost() {
        Permanent ally = addCreatureReady(player1, new HuntmasterLiger());

        harness.castFromHand(player1, new HuntmasterLiger(), "{3}{W}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Huntmaster Liger")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(4);
    }

    private void triggerMutation(Permanent source) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, source, List.of(source.getCard()), player1.getId()));
    }
}
