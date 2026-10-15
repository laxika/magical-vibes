package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideIsland.class, MetallicSliver.class, GrizzlyBears.class})
class RiptideIslandTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new RiptideIsland(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkToAndUpkeepCreateTwoColorlessSlivers() {
        triggerPlaneswalkTo();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Sliver");
        assertThat(tokens).hasSize(4);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SLIVER);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    void chaosBoostsAndGivesHasteOnlyToOwnSlivers() {
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent ownNonSliver = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSliver = addCreatureReady(player2, new MetallicSliver());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(ownSliver.getEffectivePower()).isEqualTo(2);
        assertThat(ownSliver.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.HASTE)).isTrue();
        assertThat(ownNonSliver.getEffectivePower()).isEqualTo(2);
        assertThat(ownNonSliver.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownNonSliver, Keyword.HASTE)).isFalse();
        assertThat(opposingSliver.getEffectivePower()).isEqualTo(1);
        assertThat(opposingSliver.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(ownSliver.getEffectivePower()).isEqualTo(1);
        assertThat(ownSliver.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    void upkeepCreatesTokensForTheNewActivePlayer() {
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Sliver")).isEqualTo(2);
        assertThat(countPermanents(player1, "Sliver")).isZero();
    }

    @Test
    void chaosCountsSliversAtResolutionAndLocksTheBonusAndRecipients() {
        Permanent first = addCreatureReady(player1, new MetallicSliver());
        harness.inMutationScope(() -> planar.chaos(gd));
        Permanent second = addCreatureReady(player1, new MetallicSliver());
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();

        Permanent lateArrival = addCreatureReady(player1, new MetallicSliver());
        assertThat(lateArrival.getEffectivePower()).isEqualTo(1);
        assertThat(lateArrival.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, lateArrival, Keyword.HASTE)).isFalse();
        assertThat(first.getEffectivePower()).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
    }

    @Test
    void chaosWithNoSliversDoesNotAffectSliversEnteringLater() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        Permanent sliver = addCreatureReady(player1, new MetallicSliver());
        assertThat(sliver.getEffectivePower()).isEqualTo(1);
        assertThat(sliver.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isFalse();
    }

    private void triggerPlaneswalkTo() {
        harness.inMutationScope(() -> planar.trigger(gd, source,
                com.github.laxika.magicalvibes.model.EffectSlot.PLANESWALK_TO_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
    }
}
