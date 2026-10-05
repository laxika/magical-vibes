package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PendantOfProsperity.class, Forest.class, SolRing.class})
class PendantOfProsperityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters under an opponent's control")
    void entersUnderOpponentsControl() {
        harness.castFromHand(player1, new PendantOfProsperity(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pendant of Prosperity");
        harness.assertOnBattlefield(player2, "Pendant of Prosperity");
    }

    @Test
    @DisplayName("Lets the controller and owner draw and put lands from their hands onto the battlefield")
    void controllerAndOwnerResolveTheirChoices() {
        PendantOfProsperity pendant = new PendantOfProsperity();
        pendant.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, pendant);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Card ownerDraw = new SolRing();
        Card controllerDraw = new SolRing();
        harness.setLibrary(player1, List.of(ownerDraw));
        harness.setLibrary(player2, List.of(controllerDraw));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).contains(ownerDraw);
        assertThat(gd.playerHands.get(player2.getId())).contains(controllerDraw);
    }

    @Test
    @DisplayName("Enters under the opponent's control without a control-change trigger")
    void opponentControlsPendantImmediatelyAfterSpellResolves() {
        harness.castFromHand(player1, new PendantOfProsperity(), "{3}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pendant of Prosperity");
        harness.assertOnBattlefield(player2, "Pendant of Prosperity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both players can decline lands without skipping either draw")
    void bothPlayersDeclineTheirLandChoices() {
        PendantOfProsperity pendant = new PendantOfProsperity();
        pendant.setOwnerId(player1.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, pendant);
        Card ownerLand = new Forest();
        Card controllerLand = new Forest();
        harness.setHand(player1, List.of(ownerLand));
        harness.setHand(player2, List.of(controllerLand));
        Card ownerDraw = new SolRing();
        Card controllerDraw = new SolRing();
        harness.setLibrary(player1, List.of(ownerDraw));
        harness.setLibrary(player2, List.of(controllerDraw));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        assertThat(permanent.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(controllerLand, controllerDraw);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownerLand);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownerLand, ownerDraw);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Each player may put the land they just drew onto the battlefield")
    void newlyDrawnLandsCanBeChosen() {
        PendantOfProsperity pendant = new PendantOfProsperity();
        pendant.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, pendant);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest).allMatch(p -> !p.isTapped());
    }

    @Test
    @DisplayName("Having no lands to put onto the battlefield does not stop the owner's half")
    void noEligibleLandsStillAllowsBothPlayersToDraw() {
        PendantOfProsperity pendant = new PendantOfProsperity();
        pendant.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, pendant);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card ownerDraw = new SolRing();
        Card controllerDraw = new SolRing();
        harness.setLibrary(player1, List.of(ownerDraw));
        harness.setLibrary(player2, List.of(controllerDraw));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(controllerDraw);
        harness.assertNotOnBattlefield(player1, "Sol Ring");
        harness.assertNotOnBattlefield(player2, "Sol Ring");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An owner who controls Pendant draws twice and may put two lands onto the battlefield")
    void ownerControllerResolvesBothHalvesInOrder() {
        PendantOfProsperity pendant = new PendantOfProsperity();
        pendant.setOwnerId(player1.getId());
        harness.addToBattlefield(player1, pendant);
        harness.setHand(player1, List.of());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondLand);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
