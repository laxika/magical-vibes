package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RacersScoreboard.class, Forest.class, GrizzlyBears.class})
class RacersScoreboardTest extends BaseCardTest {

    @Test
    void entersDrawsTwoThenDiscardsOne() {
        Card drawnOne = new Forest();
        Card drawnTwo = new Forest();
        Card discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        harness.setHand(player1, List.of(new RacersScoreboard(), discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnOne, drawnTwo);
    }

    @Test
    void spellsCostOneLessAtMaxSpeed() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void spellsDoNotCostLessBelowMaxSpeed() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringStartsEnginesAndAllowsDiscardingADrawnCard() {
        Card drawnOne = new Forest();
        Card drawnTwo = new Forest();
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        harness.setHand(player1, List.of(new RacersScoreboard()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnOne);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnTwo);
    }

    @Test
    void enteringDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new RacersScoreboard()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void artifactSpellsAlsoCostOneLessAtMaxSpeed() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new RacersScoreboard()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void multipleScoreboardsStackTheirCostReductions() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        harness.addToBattlefield(player1, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new RacersScoreboard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void costReductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        harness.addToBattlefield(player1, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsScoreboardDoesNotReduceYourCosts() {
        harness.addToBattlefield(player2, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 4);
        gd.playerSpeeds.put(player2.getId(), 4);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsMaxSpeedDoesNotEnableYourScoreboard() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentLifeLossIncreasesSpeedOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        harness.runStateBasedActions();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 1, "Opponent life loss"));
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 1, "Further opponent life loss"));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void reachingMaxSpeedImmediatelyEnablesCostReduction() {
        harness.addToBattlefield(player1, new RacersScoreboard());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new RacersScoreboard()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 1, "Opponent life loss"));
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }
}
