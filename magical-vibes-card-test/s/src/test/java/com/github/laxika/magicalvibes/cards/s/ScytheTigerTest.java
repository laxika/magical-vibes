package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScytheTiger.class, Mountain.class, BurstLightning.class})
class ScytheTigerTest extends BaseCardTest {

    private void castScytheTiger() {
        harness.setHand(player1, List.of(new ScytheTiger()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Auto-sacrifices when its controller has no land")
    void autoSacrificesWithoutLand() {
        castScytheTiger();

        harness.assertNotOnBattlefield(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Scythe Tiger");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the land sacrifice keeps Scythe Tiger")
    void sacrificingLandKeepsScytheTiger() {
        harness.addToBattlefield(player1, new Mountain());
        castScytheTiger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mountain"));

        harness.assertOnBattlefield(player1, "Scythe Tiger");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining the land sacrifice sacrifices Scythe Tiger")
    void decliningSacrificesScytheTiger() {
        harness.addToBattlefield(player1, new Mountain());
        castScytheTiger();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Scythe Tiger");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Opponent-controlled lands do not satisfy the ability")
    void opponentLandDoesNotCount() {
        harness.addToBattlefield(player2, new Mountain());
        castScytheTiger();

        harness.assertNotOnBattlefield(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Scythe Tiger");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonland permanent cannot satisfy the sacrifice")
    void nonlandDoesNotCount() {
        var existingTiger = harness.addToBattlefieldAndReturn(player1, new ScytheTiger());
        castScytheTiger();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(existingTiger);
        harness.assertInGraveyard(player1, "Scythe Tiger");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may choose a tapped land and sacrifices only that land")
    void choosesOneTappedLand() {
        var unchosenLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        var chosenLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        chosenLand.tap();
        castScytheTiger();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosenLand.getId());

        harness.assertOnBattlefield(player1, "Scythe Tiger");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosenLand).doesNotContain(chosenLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(chosenLand.getCard());
    }

    @Test
    @DisplayName("The sacrifice is a triggered ability and waits on the stack")
    void sacrificeWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new ScytheTiger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scythe Tiger");
        harness.assertNotInGraveyard(player1, "Scythe Tiger");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Scythe Tiger");
    }

    @Test
    @DisplayName("Shroud prevents its controller from targeting Scythe Tiger")
    void controllerCannotTargetScytheTiger() {
        var tiger = harness.addToBattlefieldAndReturn(player1, new ScytheTiger());
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, tiger.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents an opponent from targeting Scythe Tiger")
    void opponentCannotTargetScytheTiger() {
        var tiger = harness.addToBattlefieldAndReturn(player2, new ScytheTiger());
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, tiger.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("An old enters trigger cannot sacrifice a Scythe Tiger that left and returned")
    void oldTriggerCannotSacrificeReturnedTiger() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new ScytheTiger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        var originalTiger = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Scythe Tiger"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, originalTiger));
        var returningCard = gd.playerGraveyards.get(player1.getId()).remove(0);
        var returnedTiger = harness.enterBattlefieldAndReturn(player1, returningCard);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mountain"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(returnedTiger);
        harness.assertNotInGraveyard(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Mountain");
    }
}
