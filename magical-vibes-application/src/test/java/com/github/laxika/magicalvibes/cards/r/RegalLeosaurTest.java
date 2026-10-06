package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalLeosaur.class, GrizzlyBears.class, AlmightyBrushwagg.class})
class RegalLeosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating Regal Leosaur gives other creatures you control +2/+1 until end of turn")
    void mutationBoostsOtherOwnCreaturesUntilEndOfTurn() {
        Permanent leosaur = addCreatureReady(player1, new RegalLeosaur());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        mutate(leosaur);

        assertThat(gqs.getEffectivePower(gd, leosaur)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leosaur)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting for the hybrid mutate cost merges with the host and boosts only other creatures")
    void mutateCastMergesAndBoostsOtherCreatures() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent other = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new RegalLeosaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, host.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, host)).isIn(1, 2);
        assertThat(gqs.getEffectiveToughness(gd, host))
                .isEqualTo(gqs.getEffectivePower(gd, host));
    }

    @Test
    @DisplayName("Casting normally does not boost other creatures")
    void normalCastDoesNotBoostOtherCreatures() {
        Permanent other = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new RegalLeosaur()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated mutations stack their boosts but do not boost creatures entering afterward")
    void repeatedMutationsStackAndDoNotAffectLaterCreatures() {
        Permanent leosaur = addCreatureReady(player1, new RegalLeosaur());
        Permanent other = addCreatureReady(player1, new AlmightyBrushwagg());

        mutate(leosaur);
        mutate(leosaur);
        Permanent lateArrival = addCreatureReady(player1, new AlmightyBrushwagg());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, leosaur)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leosaur)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lateArrival)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lateArrival)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mutation trigger resolves even after Regal Leosaur leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent leosaur = addCreatureReady(player1, new RegalLeosaur());
        Permanent other = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, leosaur, List.of(leosaur.getCard()), player1.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(leosaur);
        gd.playerGraveyards.get(player1.getId()).add(leosaur.getCard());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    private void mutate(Permanent creature) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, creature, List.of(creature.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
