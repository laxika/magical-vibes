package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSurveyor.class})
class GoblinSurveyorTest extends BaseCardTest {

    @Test
    void maxSpeedAbilityExilesThisCardAndDraws() {
        GoblinSurveyor surveyor = new GoblinSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(new GoblinSurveyor()));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GoblinSurveyor);
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeed() {
        harness.setGraveyard(player1, List.of(new GoblinSurveyor()));
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
    }

    @Test
    void exileIsPaidBeforeTheDrawResolves() {
        GoblinSurveyor surveyor = new GoblinSurveyor();
        GoblinSurveyor drawnCard = new GoblinSurveyor();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(surveyor));
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == surveyor);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == surveyor);
    }

    @Test
    void abilityCanBeActivatedDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new GoblinSurveyor()));
        GoblinSurveyor drawnCard = new GoblinSurveyor();
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void insufficientManaDoesNotExileTheCard() {
        GoblinSurveyor surveyor = new GoblinSurveyor();
        harness.setGraveyard(player1, List.of(surveyor));
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(surveyor);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringBattlefieldStartsSpeedButDoesNotResetExistingSpeed() {
        harness.addToBattlefield(player1, new GoblinSurveyor());
        harness.runStateBasedActions();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new GoblinSurveyor());
        harness.runStateBasedActions();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void trampleDealsExcessDamageAndIncreasesSpeed() {
        addCreatureReady(player1, new GoblinSurveyor());
        var blocker = addCreatureReady(player2, new GoblinSurveyor());
        harness.runStateBasedActions();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Goblin Surveyor");
        harness.assertInGraveyard(player2, "Goblin Surveyor");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }
}
