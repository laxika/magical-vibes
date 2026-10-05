package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninSurveyor.class})
class LeoninSurveyorTest extends BaseCardTest {

    @Test
    void hasFirstStrikeDuringItsControllersTurnOnly() {
        Permanent surveyor = addCreatureReady(player1, new LeoninSurveyor());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, surveyor, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, surveyor, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerTurn() {
        addCreatureReady(player1, new LeoninSurveyor());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        resolveAllTriggers();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void maxSpeedAbilityExilesThisCardAndDraws() {
        LeoninSurveyor surveyor = new LeoninSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(new LeoninSurveyor()));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(surveyor.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof LeoninSurveyor);
    }

    @Test
    void cannotActivateBelowMaxSpeed() {
        LeoninSurveyor surveyor = new LeoninSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int speed = 0; speed < 4; speed++) {
            gd.playerSpeeds.put(player1.getId(), speed);
            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(surveyor);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void canActivateDuringOpponentsTurnAtMaxSpeed() {
        LeoninSurveyor surveyor = new LeoninSurveyor();
        LeoninSurveyor drawnCard = new LeoninSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void lifeLossDuringOpponentsTurnDoesNotIncreaseYourSpeed() {
        addCreatureReady(player1, new LeoninSurveyor());
        harness.runStateBasedActions();
        harness.forceActivePlayer(player2);

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }
}
