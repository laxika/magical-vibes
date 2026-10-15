package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DestructiveFlow.class, Forest.class, MeteorCrater.class})
class DestructiveFlowTest extends BaseCardTest {

    @Test
    @DisplayName("At each player's upkeep that player sacrifices a nonbasic land")
    void sacrificesNonbasicLandAtEachPlayersUpkeep() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(basicLand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(nonbasicLand.getId()));
    }

    @Test
    @DisplayName("The controller also sacrifices a nonbasic land on their own upkeep")
    void triggersOnControllersUpkeep() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(nonbasicLand.getId()));
    }

    @Test
    @DisplayName("With multiple nonbasic lands the player chooses which one to sacrifice")
    void choosesAmongMultipleNonbasicLands() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Does not prompt when the active player controls no nonbasic land")
    void doesNothingWhenOnlyBasicLandIsAvailable() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(basicLand.getId()));
    }

    @Test
    @DisplayName("Only the active player sacrifices, even when both players have nonbasic lands")
    void leavesInactivePlayersNonbasicLandUntouched() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        harness.addToBattlefield(player1, new MeteorCrater());
        harness.addToBattlefield(player2, new MeteorCrater());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Meteor Crater");
        harness.assertNotInGraveyard(player1, "Meteor Crater");
        harness.assertNotOnBattlefield(player2, "Meteor Crater");
        harness.assertInGraveyard(player2, "Meteor Crater");
    }

    @Test
    @DisplayName("With no lands the active player sacrifices nothing, even if the controller has a nonbasic land")
    void doesNothingWhenActivePlayersBattlefieldIsEmpty() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        harness.addToBattlefield(player1, new MeteorCrater());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Meteor Crater");
        harness.assertOnBattlefield(player1, "Destructive Flow");
    }

    @Test
    @DisplayName("Two Destructive Flows each require a separate nonbasic land sacrifice")
    void multipleCopiesTriggerSeparately() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());
        harness.addToBattlefield(player2, new MeteorCrater());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Meteor Crater");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof MeteorCrater)
                .hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
