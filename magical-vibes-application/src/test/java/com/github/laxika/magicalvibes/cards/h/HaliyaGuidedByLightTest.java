package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaliyaGuidedByLight.class, GrizzlyBears.class, LotusPetal.class, Memnite.class})
class HaliyaGuidedByLightTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life when Haliya and another creature enter under its controller's control")
    void gainsLifeForSelfAndAllyCreatureEntries() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HaliyaGuidedByLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains life when a noncreature artifact enters under its controller's control")
    void gainsLifeForNoncreatureArtifactEntry() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HaliyaGuidedByLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LotusPetal()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An artifact creature entering triggers Haliya only once")
    void artifactCreatureTriggersOnlyOnce() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HaliyaGuidedByLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Memnite()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Draws a card at its controller's end step after gaining three life")
    void drawsAtEndStepAfterGainingThreeLife() {
        harness.addToBattlefield(player1, new HaliyaGuidedByLight());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw at the end step after gaining fewer than three life")
    void doesNotDrawAtEndStepBelowThreshold() {
        harness.addToBattlefield(player1, new HaliyaGuidedByLight());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Warp casts Haliya for {W} and exiles it at the next end step")
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        HaliyaGuidedByLight haliya = new HaliyaGuidedByLight();
        harness.setHand(player1, List.of(haliya));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        org.assertj.core.api.Assertions.assertThat(gd.findExiledCard(haliya.getId())).isNotNull();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
