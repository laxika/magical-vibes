package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoxodonSurveyor.class})
class LoxodonSurveyorTest extends BaseCardTest {

    @Test
    void maxSpeedAbilityExilesThisCardAndDraws() {
        LoxodonSurveyor surveyor = new LoxodonSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(new LoxodonSurveyor()));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Loxodon Surveyor");
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeed() {
        harness.setGraveyard(player1, List.of(new LoxodonSurveyor()));
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void opponentsMaxSpeedDoesNotAllowActivationAtLowerSpeed(int speed) {
        LoxodonSurveyor surveyor = new LoxodonSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        gd.playerSpeeds.put(player1.getId(), speed);
        gd.playerSpeeds.put(player2.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(surveyor);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotExileTheCard() {
        LoxodonSurveyor surveyor = new LoxodonSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(surveyor);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilingIsPaidImmediatelyButDrawingWaitsForResolution() {
        LoxodonSurveyor surveyor = new LoxodonSurveyor();
        LoxodonSurveyor drawnCard = new LoxodonSurveyor();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == surveyor);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void abilityCanBeActivatedOnOpponentsTurn() {
        LoxodonSurveyor drawnCard = new LoxodonSurveyor();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new LoxodonSurveyor()));
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringBattlefieldStartsEngines() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new LoxodonSurveyor(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loxodon Surveyor");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void enteringBattlefieldDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new LoxodonSurveyor(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loxodon Surveyor");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void opponentLifeLossIncreasesSpeedOnlyOnceDuringOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new LoxodonSurveyor(), "{2}{G}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, null));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, null));
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, null));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playersWhoseSpeedIncreasedThisTurn.clear();
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, null));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
