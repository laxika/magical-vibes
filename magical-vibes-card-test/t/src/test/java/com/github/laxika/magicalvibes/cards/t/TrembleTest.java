package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tremble.class, Mountain.class, Forest.class, AvenFlock.class})
class TrembleTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices a land when they control only one")
    void eachPlayerSacrificesOneLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        cast();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A player with no lands sacrifices nothing")
    void playerWithNoLandsSacrificesNothing() {
        harness.addToBattlefield(player2, new Forest());

        cast();

        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Each player chooses which land to sacrifice")
    void eachPlayerChoosesLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        cast();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player1, "Mountain")));

        choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(harness.getPermanentId(player2, "Forest")));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Only lands are sacrificed")
    void onlyLandsAreSacrificed() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new AvenFlock());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new AvenFlock());

        cast();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Aven Flock");
        harness.assertOnBattlefield(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Neither land is sacrificed until both players have chosen")
    void sacrificesWaitForBothChoices() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player1, "Mountain")));

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player1, "Mountain");

        harness.handleMultiplePermanentsChosen(player2, List.of(harness.getPermanentId(player2, "Forest")));

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A forced sole-land sacrifice waits for the other player's choice")
    void soleLandWaitsForOtherPlayerChoice() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        cast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");

        harness.handleMultiplePermanentsChosen(player2, List.of(harness.getPermanentId(player2, "Forest")));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Tremble resolves without choices when neither player controls a land")
    void neitherPlayerHasLands() {
        harness.addToBattlefield(player1, new AvenFlock());
        harness.addToBattlefield(player2, new AvenFlock());

        cast();

        harness.assertOnBattlefield(player1, "Aven Flock");
        harness.assertOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player1, "Tremble");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void cast() {
        harness.castFromHand(player1, new Tremble(), "{1}{R}");
        harness.passBothPriorities();
    }
}
