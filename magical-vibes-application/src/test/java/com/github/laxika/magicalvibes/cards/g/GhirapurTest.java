package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DeluxeDragster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IchorElixir;
import com.github.laxika.magicalvibes.cards.p.PalladiumMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ghirapur.class, DeluxeDragster.class, Forest.class, IchorElixir.class, PalladiumMyr.class})
class GhirapurTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Ghirapur(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void beginningOfCombatAnimatesOnlyYourEligibleArtifacts() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorElixir());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new PalladiumMyr());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DeluxeDragster());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IchorElixir());

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleBeginningOfCombatTriggers(gd));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, artifact))
                .anyMatch(ability -> "Crew 2".equals(ability.getDescription()));

        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isNotEqualTo(5);
        assertThat(gs.getEffectiveActivatedAbilities(gd, artifactCreature))
                .noneMatch(ability -> "Crew 2".equals(ability.getDescription()));
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opponentArtifact))
                .noneMatch(ability -> "Crew 2".equals(ability.getDescription()));
    }

    @Test
    void chaosReturnsTargetNoncreatureArtifactFromYourGraveyard() {
        Card land = new Forest();
        Card artifactCreature = new PalladiumMyr();
        Card artifact = new IchorElixir();
        harness.setGraveyard(player1, List.of(land, artifactCreature, artifact));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellGraveyardTargetTrigger(gd));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).containsExactly(artifact);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ichor Elixir");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, artifactCreature);
    }
}
