package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CemeteryGatekeeper.class, Forest.class, GrizzlyBears.class, PullFromEternity.class})
class CemeteryGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability exiles and remembers a graveyard card")
    void exilesAndImprintsChosenCard() {
        Card exiled = new GrizzlyBears();
        Permanent gatekeeper = enterGatekeeperWith(exiled);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
        assertThat(gd.getImprintedCard(gatekeeper.getCard())).isSameAs(exiled);
    }

    @Test
    @DisplayName("A matching spell deals damage to the player who cast it")
    void matchingSpellDamagesCaster() {
        enterGatekeeperWith(new GrizzlyBears());
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("A matching land deals damage to the player who played it")
    void matchingLandDamagesPlayerWhoPlayedIt() {
        enterGatekeeperWith(new Forest());
        playLand(player2, new Forest());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A matching land played by its controller deals damage to that player")
    void matchingLandDamagesController() {
        enterGatekeeperWith(new Forest());
        playLand(player1, new Forest());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A land with no shared card type does not deal damage")
    void nonmatchingLandDoesNotDealDamage() {
        enterGatekeeperWith(new GrizzlyBears());
        playLand(player1, new Forest());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A matching spell cast by its controller damages that controller")
    void matchingSpellDamagesController() {
        enterGatekeeperWith(new CemeteryGatekeeper());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new CemeteryGatekeeper(), "{1}{R}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature spell does not match an exiled land")
    void nonmatchingSpellDoesNotDealDamage() {
        enterGatekeeperWith(new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CemeteryGatekeeper(), "{1}{R}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Empty graveyards leave Gatekeeper unable to match a land")
    void emptyGraveyardsDoNotEnableDamage() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new CemeteryGatekeeper());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        playLand(player2, new Forest());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A matching land entering without being played does not trigger damage")
    void landEnteringWithoutBeingPlayedDoesNotDealDamage() {
        enterGatekeeperWith(new Forest());

        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The controller can choose a card from their own graveyard")
    void exilesCardFromControllersGraveyard() {
        Card chosen = new Forest();
        harness.setGraveyard(player1, List.of(chosen));
        harness.setGraveyard(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new CemeteryGatekeeper());
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A card that has left exile cannot match later land plays")
    void cardLeavingExileStopsFutureTriggers() {
        Card exiled = new Forest();
        enterGatekeeperWith(exiled);
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, exiled.getId());
        resolveAllTriggers();
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiled);

        playLand(player2, new Forest());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The shared-type condition is checked again when damage resolves")
    void removingExiledCardInResponseStopsPendingDamage() {
        Card exiled = new Forest();
        enterGatekeeperWith(exiled);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest(), new PullFromEternity()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, exiled.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiled);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Removing the exiled card in response also stops spell-triggered damage")
    void removingExiledCardStopsPendingSpellDamage() {
        Card exiled = new CemeteryGatekeeper();
        enterGatekeeperWith(exiled);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).hasSize(2);
        harness.setHand(player2, List.of(new PullFromEternity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, exiled.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiled);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent enterGatekeeperWith(Card card) {
        harness.setGraveyard(player2, List.of(card));
        Permanent gatekeeper = harness.enterBattlefieldAndReturn(player1, new CemeteryGatekeeper());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        return gatekeeper;
    }

    private void playLand(com.github.laxika.magicalvibes.model.Player player, Card land) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(land));
        harness.playLand(player, 0);
        harness.passBothPriorities();
    }
}
