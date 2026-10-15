package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NewNewYork.class, AccordersShield.class, GrizzlyBears.class})
class NewNewYorkTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new NewNewYork(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void beginningOfCombatAnimatesOwnNoncreatureArtifactsUntilEndOfTurn() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);

        assertThat(gqs.getEffectiveCardTypes(gd, ownArtifact))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownArtifact)).contains(CardSubtype.VEHICLE);
        assertThat(gqs.getEffectivePower(gd, ownArtifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifact)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.HASTE)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownArtifact))
                .anyMatch(ability -> "Crew 1".equals(ability.getDescription()));

        assertThat(gqs.isCreature(gd, opposingArtifact)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownArtifact)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownArtifact))
                .noneMatch(ability -> "Crew 1".equals(ability.getDescription()));
    }

    @Test
    void chaosCreatesATreasureAndAnAlien() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Alien")).singleElement().satisfies(alien -> {
            assertThat(gqs.isCreature(gd, alien)).isTrue();
            assertThat(gqs.getEffectivePower(gd, alien)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, alien)).isEqualTo(2);
        });
    }
}
