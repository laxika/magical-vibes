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
        var diver = addCreatureReady(player1, new RootwaterDiver());
        Card artifact = new CursedScroll();
        harness.setGraveyard(player1, List.of(artifact));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(diver.getCard().getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(artifact.getId()));
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
}
