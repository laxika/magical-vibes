package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlumbTheForbidden;
import com.github.laxika.magicalvibes.cards.p.PrismariCampus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpressiveIteration.class, GrizzlyBears.class, PrismariCampus.class, PlumbTheForbidden.class})
class ExpressiveIterationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one card into hand, one on the bottom, and exiles one playable this turn")
    void distributesThreeCards() {
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card first = deck.get(0);
        Card second = deck.get(1);
        Card third = deck.get(2);
        int initialDeckSize = deck.size();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandBottomExileChoice.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandBottomExile(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(deck).doesNotContain(first, third);
        assertThat(deck).contains(second);
        assertThat(deck.get(deck.size() - 1)).isSameAs(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(third);
        assertThat(gd.exilePlayPermissions.get(third.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(third.getId());
        assertThat(deck).hasSize(initialDeckSize - 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With two library cards, puts one into hand and one on the bottom")
    void distributesTwoCards() {
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandBottomExile(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(deck).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("End-of-turn cleanup removes the exiled card's play permission")
    void permissionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        Card exiled = gd.playerDecks.get(player1.getId()).get(2);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandBottomExile(0, 1));

        assertThat(gd.exilePlayPermissions).containsKey(exiled.getId());
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void oneLibraryCardGoesIntoHandWithoutAChoice() {
        Card onlyCard = new ExpressiveIteration();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotPromptOrCauseADrawLoss() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void exiledSpellRequiresItsManaCost() {
        Card exiled = exileWithIteration(new ExpressiveIteration());

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.HandBottomExile(0, 1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiled);
    }

    @Test
    void exiledSorceryStillRequiresSorceryTiming() {
        Card exiled = exileWithIteration(new ExpressiveIteration());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void unplayedCardRemainsExiledAndCannotBeCastAfterCleanup() {
        Card exiled = exileWithIteration(new ExpressiveIteration());
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void exiledLandCanBePlayedWithAnAvailableLandPlay() {
        Card exiled = exileWithIteration(new PrismariCampus());

        harness.castFromExile(player1, exiled.getId());

        harness.assertOnBattlefield(player1, "Prismari Campus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void exiledLandDoesNotGrantAnAdditionalLandPlay() {
        Card exiled = exileWithIteration(new PrismariCampus());
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
        harness.assertNotOnBattlefield(player1, "Prismari Campus");
    }

    @Test
    void exiledPlumbTheForbiddenCanBeCastWithoutSacrificingCreatures() {
        Card exiled = exileWithIteration(new PlumbTheForbidden());
        Card remaining = gd.playerDecks.get(player1.getId()).getFirst();
        int initialLife = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiled);
        harness.assertLife(player1, initialLife - 1);
    }

    private Card exileWithIteration(Card exiled) {
        harness.setLibrary(player1, List.of(new ExpressiveIteration(), new ExpressiveIteration(),
                exiled, new ExpressiveIteration()));
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.HandBottomExile(0, 1));
        return exiled;
    }
}
