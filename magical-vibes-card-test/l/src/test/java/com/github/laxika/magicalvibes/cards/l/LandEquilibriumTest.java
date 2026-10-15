package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.y.YavimayaDryad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LandEquilibrium.class, Forest.class, Island.class, YavimayaDryad.class})
class LandEquilibriumTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent sacrifices a chosen land after putting a land onto the battlefield")
    void opponentSacrificesChosenLandAfterPlayingLand() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new Island()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).context())
                .isInstanceOf(PermanentChoiceContext.LandEquilibriumSacrifice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId(),
                        gd.playerBattlefields.get(player2.getId()).get(2).getId());

        harness.handlePermanentChosen(player2, firstLand.getId());

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(secondLand)
                .noneMatch(permanent -> permanent.getId().equals(firstLand.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The replacement does not apply when the opponent has fewer lands before entry")
    void doesNotApplyWhenOpponentHasFewerLands() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new Island()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The replacement does not apply to a nonland permanent")
    void doesNotApplyToNonlandPermanent() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.enterBattlefieldAndReturn(player2, new LandEquilibrium());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The entering land can be sacrificed when it is the only land available")
    void sacrificesEnteringLandWhenItIsTheOnlyLandAvailable() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.setHand(player2, List.of(new Island()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The opponent sacrifices a land even when the entering land is put under another player's control")
    void appliesToPlayerPuttingLandUnderAnotherPlayersControl() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new YavimayaDryad()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3)
                .filteredOn(permanent -> permanent.getCard() instanceof Forest)
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("The controller may put a land under an opponent's control without a sacrifice")
    void controllerPuttingLandUnderOpponentsControlDoesNotApply() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new YavimayaDryad()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The opponent can choose the entering land instead of an existing land")
    void opponentCanChooseEnteringLand() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        Permanent existing = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new Island()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Island"));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(existing);
        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each copy requires a sacrifice even after the first sacrifice lowers the land count")
    void multipleCopiesEachRequireSacrifice() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new Island()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Island"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Island");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
