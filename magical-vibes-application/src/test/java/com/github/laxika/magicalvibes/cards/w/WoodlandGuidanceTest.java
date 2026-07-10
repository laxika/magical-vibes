package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WoodlandGuidanceTest extends BaseCardTest {

    private Card prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card graveyardCard = new GrizzlyBears();
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
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash returns the card, untaps all Forests, and exiles Woodland Guidance")
    void wonClashUntapsForests() {
        Card graveyardCard = prepare();
        List<Permanent> forests = addTappedForests();
        stackClashWinForCaster();

        harness.castSorcery(player1, 0, graveyardCard.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
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
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));

        harness.castSorcery(player1, 0, graveyardCard.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(graveyardCard.getId()));
        assertThat(forests).allMatch(Permanent::isTapped);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Woodland Guidance"));
    }
}
