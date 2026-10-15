package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BehindTheMask.class, FountainOfYouth.class, GrizzlyBears.class, Island.class})
class BehindTheMaskTest extends BaseCardTest {

    @Test
    void withoutEvidenceMakesCreatureAnArtifactCreatureWithBasePowerAndToughnessFourThree() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BehindTheMask()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void collectingEvidenceMakesArtifactTargetAnArtifactCreatureWithBasePowerAndToughnessOneOne() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        BehindTheMask spell = new BehindTheMask();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstantWithMultipleGraveyardExile(
                player1, 0, target.getId(), List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void cannotTargetAPermanentThatIsNeitherAnArtifactNorACreature() {
        Permanent validTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new BehindTheMask()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(validTarget, invalidTarget);
    }

    @Test
    void evidenceCanBeDeclinedEvenWithEnoughManaValueInGraveyard() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BehindTheMask()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void withoutEvidenceAnimatesNoncreatureArtifactOnlyUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BehindTheMask()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isFalse();
    }

    @Test
    void cannotCollectEvidenceWithTotalManaValueBelowSix() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new Island());
        harness.setGraveyard(player1, evidence);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        BehindTheMask spell = new BehindTheMask();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, target.getId(), List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void evidenceCanExceedSixAndChangesCreatureBaseStatsWithoutRemovingCounters() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new BehindTheMask()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }
}
