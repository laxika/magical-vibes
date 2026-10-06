package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwaterDiver.class, CursedScroll.class, FightingDrake.class})
class RootwaterDiverTest extends BaseCardTest {

    @Test
    @DisplayName("Returns targeted artifact card from graveyard to hand and sacrifices itself")
    void returnsArtifactFromGraveyardToHand() {
        addCreatureReady(player1, new RootwaterDiver());
        Card artifact = new CursedScroll();
        harness.setGraveyard(player1, List.of(artifact));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.assertInGraveyard(player1, "Rootwater Diver");
        resolveAllTriggers();

        harness.assertInHand(player1, "Cursed Scroll");
        harness.assertNotInGraveyard(player1, "Cursed Scroll");
    }

    @Test
    @DisplayName("Cannot target a nonartifact card in the graveyard")
    void cannotTargetNonArtifact() {
        var diver = addCreatureReady(player1, new RootwaterDiver());
        Card nonArtifact = new FightingDrake();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(diver.getId()));
    }

    @Test
    @DisplayName("Cannot target an artifact card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        var diver = addCreatureReady(player1, new RootwaterDiver());
        Card artifact = new CursedScroll();
        harness.setGraveyard(player2, List.of(artifact));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(diver.getId()));
    }

    @Test
    @DisplayName("Cannot activate the ability while Rootwater Diver is tapped")
    void cannotActivateWhileTapped() {
        var diver = addCreatureReady(player1, new RootwaterDiver());
        diver.tap();
        Card artifact = new CursedScroll();
        harness.setGraveyard(player1, List.of(artifact));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(diver.getId()));
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        var diver = harness.addToBattlefieldAndReturn(player1, new RootwaterDiver());
        diver.setSummoningSick(true);
        Card artifact = new CursedScroll();
        harness.setGraveyard(player1, List.of(artifact));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(diver.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Rootwater Diver");
        harness.assertInGraveyard(player1, "Cursed Scroll");
    }

    @Test
    @DisplayName("An artifact leaving the graveyard before resolution is not returned")
    void targetLeavingGraveyardIsNotReturned() {
        addCreatureReady(player1, new RootwaterDiver());
        Card artifact = new CursedScroll();
        harness.setGraveyard(player1, List.of(artifact));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, artifact.getId());
        harness.setExile(player1, List.of(artifact));
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Cursed Scroll");
        harness.assertInGraveyard(player1, "Rootwater Diver");
        harness.assertNotOnBattlefield(player1, "Rootwater Diver");
        assertThat(gd.stack).isEmpty();
    }
}
