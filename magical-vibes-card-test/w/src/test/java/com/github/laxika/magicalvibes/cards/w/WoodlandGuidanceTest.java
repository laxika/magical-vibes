package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodlandGuidance.class, Forest.class, KithkinGreatheart.class, Island.class})
class WoodlandGuidanceTest extends BaseCardTest {

    private Card prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card graveyardCard = new KithkinGreatheart();
        harness.setGraveyard(player1, List.of(graveyardCard));

        harness.setHand(player1, List.of(new WoodlandGuidance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {3}{G}
        return graveyardCard;
    }

    private List<Permanent> addTappedForests() {
        Permanent f1 = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent f2 = harness.addToBattlefieldAndReturn(player1, new Forest());
        f1.tap();
        f2.tap();
        return List.of(f1, f2);
    }

    // Caster (player1) wins: their revealed top card has a strictly greater mana value.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash returns the card, untaps all Forests, and exiles Woodland Guidance")
    void wonClashUntapsForests() {
        Card graveyardCard = prepare();
        List<Permanent> forests = addTappedForests();
        stackClashWinForCaster();

        harness.castAndResolveSorcery(player1, 0, graveyardCard.getId());
        keepBothRevealedCards();

        // Returned to hand
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(graveyardCard.getId()));
        // Forests untapped
        assertThat(forests).allMatch(p -> !p.isTapped());
        // Woodland Guidance exiled, not in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Woodland Guidance"));
        harness.assertNotInGraveyard(player1, "Woodland Guidance");
    }

    @Test
    @DisplayName("Losing the clash still returns the card but leaves Forests tapped")
    void lostClashLeavesForestsTapped() {
        Card graveyardCard = prepare();
        List<Permanent> forests = addTappedForests();
        // Player1 loses: player2 reveals the strictly greater mana value.
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart(), new Forest(), new Forest()));

        harness.castAndResolveSorcery(player1, 0, graveyardCard.getId());
        keepBothRevealedCards();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(graveyardCard.getId()));
        assertThat(forests).allMatch(Permanent::isTapped);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Woodland Guidance"));
    }

    private void keepBothRevealedCards() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    void tiedClashReturnsCardAndExilesSpellWithoutUntapping() {
        Card graveyardCard = prepare();
        List<Permanent> forests = addTappedForests();
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        harness.castAndResolveSorcery(player1, 0, graveyardCard.getId());
        keepBothRevealedCards();

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardCard);
        assertThat(forests).allMatch(Permanent::isTapped);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof WoodlandGuidance);
        harness.assertNotInGraveyard(player1, "Woodland Guidance");
    }

    @Test
    void winningClashUntapsOnlyForestsControlledByCaster() {
        Card graveyardCard = prepare();
        List<Permanent> forests = addTappedForests();
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());
        opponentForest.tap();
        island.tap();
        creature.tap();
        stackClashWinForCaster();

        harness.castAndResolveSorcery(player1, 0, graveyardCard.getId());
        keepBothRevealedCards();

        assertThat(forests).allMatch(p -> !p.isTapped());
        assertThat(opponentForest.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void canReturnLandCard() {
        prepare();
        Card land = new Island();
        harness.setGraveyard(player1, List.of(land));
        stackClashWinForCaster();

        harness.castAndResolveSorcery(player1, 0, land.getId());
        keepBothRevealedCards();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        harness.assertNotInGraveyard(player1, "Island");
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        prepare();
        Card opponentCard = new KithkinGreatheart();
        harness.setGraveyard(player2, List.of(opponentCard));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.castSorcery(player1, 0, opponentCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removedTargetPreventsClashUntappingAndSelfExile() {
        Card graveyardCard = prepare();
        List<Permanent> forests = addTappedForests();
        stackClashWinForCaster();
        harness.castSorcery(player1, 0, graveyardCard.getId());
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(forests).allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Woodland Guidance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c instanceof WoodlandGuidance);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardCard);
    }

    @Test
    void bottomingWinningCardPreservesWinAndWaitsForBothPlacementChoices() {
        Card graveyardCard = prepare();
        List<Permanent> forests = addTappedForests();
        Card revealed = new KithkinGreatheart();
        Card next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castAndResolveSorcery(player1, 0, graveyardCard.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardCard);
        assertThat(forests).allMatch(Permanent::isTapped);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c instanceof WoodlandGuidance);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, next);

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
        assertThat(forests).allMatch(p -> !p.isTapped());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof WoodlandGuidance);
    }
}
