package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MutantSurveyor.class})
class MutantSurveyorTest extends BaseCardTest {

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerTurn() {
        addCreatureReady(player1, new MutantSurveyor());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent surveyor = addCreatureReady(player1, new MutantSurveyor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(surveyor.getPowerModifier()).isEqualTo(1);
        assertThat(surveyor.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(surveyor.getPowerModifier()).isZero();
        assertThat(surveyor.getToughnessModifier()).isZero();
    }

    @Test
    void maxSpeedAbilityExilesThisCardAndDraws() {
        MutantSurveyor surveyor = new MutantSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        MutantSurveyor drawnCard = new MutantSurveyor();
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player1.getId())).containsExactly(surveyor);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(drawnCard);
    }

    @Test
    void graveyardAbilityCannotBeActivatedBelowMaxSpeed() {
        MutantSurveyor surveyor = new MutantSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
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
    void graveyardAbilityCanBeActivatedDuringOpponentsTurn() {
        MutantSurveyor surveyor = new MutantSurveyor();
        MutantSurveyor drawnCard = new MutantSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerExiledCards.get(player1.getId())).containsExactly(surveyor);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void repeatedBoostActivationsAccumulate() {
        Permanent surveyor = addCreatureReady(player1, new MutantSurveyor());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(surveyor.getPowerModifier()).isEqualTo(2);
        assertThat(surveyor.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void graveyardAbilityCannotExileSourceWithoutEnoughMana() {
        MutantSurveyor surveyor = new MutantSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(surveyor);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingLifeOnYourOwnTurnDoesNotIncreaseYourSpeed() {
        addCreatureReady(player1, new MutantSurveyor());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1));

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
