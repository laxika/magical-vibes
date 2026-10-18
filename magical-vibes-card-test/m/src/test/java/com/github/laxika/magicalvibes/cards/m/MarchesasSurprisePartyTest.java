package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchesasSurpriseParty.class, GrizzlyBears.class})
class MarchesasSurprisePartyTest extends BaseCardTest {

    @Test
    void castingThreeSpellsDoesNotAutomaticallyTriggerAtBeginningOfEndStep() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        for (int i = 0; i < 3; i++) {
            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            harness.passBothPriorities();
        }

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(mission.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void doesNotTriggerWhenNoMissionConditionIsMet() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void nineGraveyardCardsDoNotAutomaticallyTriggerAtBeginningOfEndStep() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(mission.getId());
    }

    @Test
    void completedMissionDoesNotTriggerAgain() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        gd.faceDownCommandZoneCards.add(mission.getId());
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.faceDownCommandZoneCards).contains(mission.getId());
    }

    @Test
    void ownerSecretlyChoosesAMissionBeforeTheGame() {
        GameTestHarness openingHarness = new GameTestHarness();
        GameData openingGame = openingHarness.getGameData();
        Player owner = openingHarness.getPlayer1();
        Card mission = new MarchesasSurpriseParty();
        openingGame.playerCommandZones.get(owner.getId()).add(mission);
        openingHarness.skipMulligan();

        assertThat(openingGame.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        openingHarness.handleMayAbilityChosen(owner, true);
        assertThat(openingGame.status).isNotEqualTo(GameStatus.RUNNING);
        openingHarness.handleListChoice(owner, "There are nine or more cards in your graveyard.");

        assertThat(openingGame.secretMissionChoices).containsEntry(mission.getId(), 2);
        assertThat(openingGame.status).isEqualTo(GameStatus.RUNNING);
        assertThat(openingGame.gameLog.toString()).doesNotContain("nine or more cards");
    }

    @Test
    void revealingTheMetChosenMissionTurnsItFaceDownAndDrawsACard() {
        Card mission = chooseMission(2);
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());
        harness.passUntil(TurnStep.END_STEP);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        gs.revealSecretMission(gd, player1, mission.getId());

        assertThat(gd.faceDownCommandZoneCards).contains(mission.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void anUnchosenMissionCannotBeRevealed() {
        Card mission = chooseMission(0);
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());
        harness.passUntil(TurnStep.END_STEP);

        assertThatThrownBy(() -> gs.revealSecretMission(gd, player1, mission.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(mission.getId());
    }

    @Test
    void theMissionCanBeRevealedOnlyDuringTheEndStep() {
        Card mission = chooseMission(2);
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        assertThatThrownBy(() -> gs.revealSecretMission(gd, player1, mission.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void aRevealedMissionCannotBeRevealedAgain() {
        Card mission = chooseMission(2);
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());
        harness.passUntil(TurnStep.END_STEP);
        gs.revealSecretMission(gd, player1, mission.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.revealSecretMission(gd, player1, mission.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card chooseMission(int missionIndex) {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        gd.secretMissionChoices.put(mission.getId(), missionIndex);
        return mission;
    }
}
