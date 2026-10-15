package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TimeStretch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaseSteinRunaway.class, Forest.class, GrizzlyBears.class, Shock.class,
        LightningAxe.class, TimeStretch.class})
class ChaseSteinRunawayTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card exiles the top card until the end of the next turn")
    void discardsAndExilesTopCard() {
        Permanent chase = addCreatureReady(player1, new ChaseSteinRunaway());
        Card discarded = new GrizzlyBears();
        Card topCard = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(chase.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("The exiled card can be played from exile for its normal cost")
    void exiledCardCanBePlayed() {
        addCreatureReady(player1, new ChaseSteinRunaway());
        Card discarded = new GrizzlyBears();
        Card topCard = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void discardIsPaidBeforeTheAbilityResolves() {
        Permanent chase = addCreatureReady(player1, new ChaseSteinRunaway());
        Card discarded = new Forest();
        Card topCard = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(chase.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent chase = addCreatureReady(player1, new ChaseSteinRunaway());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chase.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent chase = harness.addToBattlefieldAndReturn(player1, new ChaseSteinRunaway());
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chase.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void emptyLibraryStillRequiresPayingTheCosts() {
        Permanent chase = addCreatureReady(player1, new ChaseSteinRunaway());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(chase.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void exiledLandCanBePlayedButDoesNotGrantAnExtraLandPlay() {
        Card topCard = new Forest();
        exileTopCard(topCard);

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void exiledLandCanBePlayedWithAnAvailableLandPlay() {
        Card topCard = new Forest();
        exileTopCard(topCard);

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void exiledSpellStillRequiresManaAndOnlyItsControllerMayPlayIt() {
        Card topCard = new Shock();
        exileTopCard(topCard);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player2, topCard.getId(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void exiledCreatureStillRequiresSorceryTiming() {
        Card topCard = new GrizzlyBears();
        exileTopCard(topCard);
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void permissionExpiresAfterTheControllersNextTurnAndCardRemainsExiled() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card topCard = new Shock();
        exileTopCard(topCard);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionExpiresAfterTheFirstOfTwoConsecutiveExtraTurns() {
        addCreatureReady(player1, new ChaseSteinRunaway());
        Card topCard = new Shock();
        harness.setHand(player1, List.of(new TimeStretch(), new Forest()));
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionSurvivesTheOpponentsExtraTurnsUntilTheControllersNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new ChaseSteinRunaway());
        Card topCard = new Shock();
        harness.setHand(player1, List.of(new TimeStretch(), new Forest()));
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void activatingDuringTheOpponentsTurnAllowsPlayOnTheControllersNextTurn() {
        harness.forceActivePlayer(player2);
        Card topCard = new Shock();
        exileTopCard(topCard);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void exiledSpellCanBeCastByPayingItsAdditionalManaCost() {
        Card topCard = new LightningAxe();
        exileTopCard(topCard);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFromExile(player1, topCard.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lightning Axe");
    }

    private void exileTopCard(Card topCard) {
        addCreatureReady(player1, new ChaseSteinRunaway());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest(), new Forest()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
