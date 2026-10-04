package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlitchGhostSurveyor.class})
class GlitchGhostSurveyorTest extends BaseCardTest {

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerTurn() {
        addCreatureReady(player1, new GlitchGhostSurveyor());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void maxSpeedAbilityExilesThisCardAndDraws() {
        GlitchGhostSurveyor surveyor = new GlitchGhostSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(new GlitchGhostSurveyor()));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GlitchGhostSurveyor);
    }

    @Test
    void cannotActivateBelowMaxSpeed() {
        GlitchGhostSurveyor surveyor = new GlitchGhostSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int speed = 0; speed < 4; speed++) {
            gd.playerSpeeds.put(player1.getId(), speed);
            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(surveyor);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void insufficientManaDoesNotExileTheCard() {
        GlitchGhostSurveyor surveyor = new GlitchGhostSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(surveyor);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateOnOpponentsTurnAndExilesAsCostBeforeDrawing() {
        GlitchGhostSurveyor surveyor = new GlitchGhostSurveyor();
        GlitchGhostSurveyor drawnCard = new GlitchGhostSurveyor();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(surveyor);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(surveyor);
    }

    @Test
    void lifeLossOnOpponentsTurnDoesNotIncreaseYourSpeed() {
        addCreatureReady(player1, new GlitchGhostSurveyor());
        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();

        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
