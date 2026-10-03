package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlastingStation.class, DrossCrocodile.class})
class BlastingStationTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and sacrifices a creature to deal 1 damage to any target")
    void sacrificesCreatureAndDealsDamage() {
        Permanent station = addReadyStation(player1);
        harness.addToBattlefield(player1, new DrossCrocodile());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(station.isTapped()).isTrue();
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("Deals 1 damage to a creature")
    void dealsDamageToCreature() {
        addReadyStation(player1);
        harness.addToBattlefield(player1, new DrossCrocodile());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        UUID targetId = target.getId();
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dross Crocodile");
        harness.assertInGraveyard(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("A creature entering under an opponent's control triggers the may untap prompt")
    void opponentCreatureEnteringTriggersMayPrompt() {
        Permanent station = addReadyStation(player1);
        station.tap();
        castCreatureFor(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A noncreature permanent entering does not trigger the may untap ability")
    void noncreatureEnteringDoesNotTriggerMayPrompt() {
        Permanent station = addReadyStation(player1);
        station.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BlastingStation(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        addReadyStation(player1);
        harness.addToBattlefield(player2, new DrossCrocodile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("A creature entering triggers the may untap prompt")
    void creatureEnteringTriggersMayPrompt() {
        addReadyStation(player1).tap();
        castCreatureFor(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may untaps Blasting Station")
    void acceptingUntapsStation() {
        Permanent station = addReadyStation(player1);
        station.tap();
        castCreatureFor(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may leaves Blasting Station tapped")
    void decliningLeavesStationTapped() {
        Permanent station = addReadyStation(player1);
        station.tap();
        castCreatureFor(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(station.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap and sacrifice costs are paid before damage resolves")
    void paysCostsBeforeResolution() {
        Permanent station = addReadyStation(player1);
        harness.addToBattlefield(player1, new DrossCrocodile());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(station.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Dross Crocodile");
        harness.assertNotOnBattlefield(player1, "Dross Crocodile");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The targeted creature may also be sacrificed to pay the cost")
    void canSacrificeTargetedCreature() {
        Permanent station = addReadyStation(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrossCrocodile());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(station.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Dross Crocodile");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Station cannot activate and does not sacrifice a creature")
    void cannotActivateWhileTapped() {
        addReadyStation(player1).tap();
        harness.addToBattlefield(player1, new DrossCrocodile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dross Crocodile");
        harness.assertNotInGraveyard(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("An opponent's creature entering can untap the Station")
    void opponentCreatureCanUntapStation() {
        Permanent station = addReadyStation(player1);
        station.tap();
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DrossCrocodile());
        otherCreature.tap();
        castCreatureFor(player2);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(station.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
    }

    private Permanent addReadyStation(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BlastingStation());
    }

    private void castCreatureFor(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new DrossCrocodile(), "{3}{B}");
        resolveAllTriggers();
    }
}
