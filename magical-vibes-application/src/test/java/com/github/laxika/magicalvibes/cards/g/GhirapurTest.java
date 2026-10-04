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

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
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
    void crewingConvertedArtifactMakesItFiveThreeAndPreservesVehicleSubtype() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorElixir());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new PalladiumMyr());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        var abilities = gs.getEffectiveActivatedAbilities(gd, artifact);
        int crewIndex = abilities.indexOf(abilities.stream()
                .filter(ability -> "Crew 2".equals(ability.getDescription()))
                .findFirst().orElseThrow());
        harness.activateAbility(player1, 0, crewIndex, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
    }

    @Test
    void artifactsEnteringAfterResolutionDoNotBecomeVehicles() {
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorElixir());

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.VEHICLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.TRAMPLE)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, artifact))
                .noneMatch(ability -> "Crew 2".equals(ability.getDescription()));
    }

    @Test
    void beginningOfCombatOnOpponentsTurnAffectsTheirArtifacts() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorElixir());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IchorElixir());
        harness.forceActivePlayer(player2);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.VEHICLE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentArtifact, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opponentArtifact))
                .anyMatch(ability -> "Crew 2".equals(ability.getDescription()));
    }

    @Test
    void vehicleConversionAndGrantedAbilitiesExpireAtEndOfTurn() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorElixir());
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.VEHICLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, artifact))
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

    @Test
    void chaosCanReturnVehicleCardWithPrintedPowerAndToughness() {
        Card vehicle = new DeluxeDragster();
        harness.setGraveyard(player1, List.of(vehicle));
        Card opponentArtifact = new IchorElixir();
        harness.setGraveyard(player2, List.of(opponentArtifact));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellGraveyardTargetTrigger(gd));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).containsExactly(vehicle);
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deluxe Dragster");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
    }

    @Test
    void chaosDoesNotReturnReplacementCardWhenTargetLeavesGraveyard() {
        Card artifact = new IchorElixir();
        Card replacement = new DeluxeDragster();
        harness.setGraveyard(player1, List.of(artifact, replacement));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellGraveyardTargetTrigger(gd));
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of(replacement));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Ichor Elixir");
        harness.assertNotInHand(player1, "Deluxe Dragster");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(replacement);
    }
}
