package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MercilessExecutioner.class, GrizzlyBears.class, GiantSpider.class})
@DisplayName("Merciless Executioner")
class MercilessExecutionerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player sacrifice their only creature automatically")
    void etbMakesEachPlayerSacrifice() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Merciless Executioner");
        harness.assertInGraveyard(player1, "Merciless Executioner");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player with multiple creatures chooses which to sacrifice")
    void playerWithMultipleCreaturesChooses() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).context())
                .isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
    }

    @Test
    @DisplayName("Choosing a creature completes the ETB sacrifice")
    void choosingCreatureCompletesSacrifice() {
        GameData gd = harness.getGameData();
        Permanent p2Bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(p2Bears.getId()));

        harness.assertInGraveyard(player1, "Merciless Executioner");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("An opponent with no creatures does not prevent the controller sacrificing")
    void opponentWithNoCreatures() {
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Merciless Executioner");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("The controller may sacrifice another creature and keep Merciless Executioner")
    void controllerChoosesAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Merciless Executioner");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Players choose in active-player order before any chosen creature is sacrificed")
    void sacrificesWaitForBothPlayersChoices() {
        Permanent controllerBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(controllerBears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentBears.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Merciless Executioner");
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new MercilessExecutioner(), "{2}{B}");
    }
}
