package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StinkweedImp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrabraskHereticPraetor.class, Forest.class, GrizzlyBears.class, StinkweedImp.class})
class UrabraskHereticPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, exiles the top card and lets you play it")
    void exilesTopCardAtYourUpkeep() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("At an opponent's upkeep, replaces that player's next draw")
    void replacesOpponentsNextDraw() {
        Card replacedCard = new GrizzlyBears();
        Card laterCard = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(replacedCard, laterCard));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(replacedCard);
        assertThat(gd.exilePlayPermissions.get(replacedCard.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(replacedCard.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(laterCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent-upkeep replacement applies only to one draw")
    void replacementAppliesOnlyOnce() {
        Card replacedCard = new GrizzlyBears();
        Card drawnCard = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(replacedCard, drawnCard));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(replacedCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    void controllerCanPlayExiledLandOnlyAtNormalLandTiming() {
        Card land = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCanCastReplacedCardWithNormalManaCostAndTiming() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(creature));
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerManaPools.get(player2.getId()).clear();
        assertThatThrownBy(() -> harness.castFromExile(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature);
    }

    @Test
    void opponentCanPlayReplacedLand() {
        Card land = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(land));
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player2, land.getId());

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(land);
    }

    @Test
    void replacedDrawDoesNotCountAsDrawingACard() {
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(new Forest()));
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        int drawsBefore = gd.cardsDrawnThisTurn.getOrDefault(player2.getId(), 0);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player2.getId(), 0)).isEqualTo(drawsBefore);
    }

    @Test
    void replacingDrawFromEmptyLibraryDoesNotLoseTheGame() {
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        var statusBefore = gd.status;

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.status).isEqualTo(statusBefore);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void playPermissionExpiresEvenWhenCardRemainsExiled() {
        Card land = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player1, List.of(land));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    @Test
    void unusedReplacementExpiresAtEndOfTurn() {
        Card card = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(card));
        harness.setHand(player2, List.of());
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));
        harness.forceActivePlayer(player1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void opponentCanChooseDredgeAndKeepUrabraskReplacementForNextDraw() {
        Card imp = new StinkweedImp();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        Card laterCard = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setGraveyard(player2, List.of(imp));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(milled.get(0), milled.get(1), milled.get(2),
                milled.get(3), milled.get(4), laterCard));
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(imp);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(laterCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(imp);
    }
}
