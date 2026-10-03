package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DamnablePact.class, Forest.class, ColossodonYearling.class, AlmsCollector.class})
class DamnablePactTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws X cards and loses X life")
    void targetPlayerDrawsAndLosesX() {
        harness.setLife(player2, 20);
        List<Card> drawnCards = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, drawnCards);
        harness.setHand(player1, List.of(new DamnablePact()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a non-player")
    void cannotTargetNonPlayer() {
        var creatureId = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling()).getId();
        harness.setHand(player1, List.of(new DamnablePact()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Caster can target themselves to draw cards and lose life")
    void canTargetCaster() {
        List<Card> drawnCards = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, drawnCards);
        harness.setHand(player1, List.of(new DamnablePact()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawnCards);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Damnable Pact");
    }

    @Test
    @DisplayName("X zero draws no cards and loses no life")
    void zeroXDoesNothing() {
        List<Card> library = List.of(new Forest());
        harness.setLibrary(player2, library);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DamnablePact()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Damnable Pact");
    }

    @Test
    @DisplayName("Alms Collector replaces the whole draw instruction without reducing life loss")
    void almsCollectorReplacesMultipleDraws() {
        Forest casterDraw = new Forest();
        Forest targetDraw = new Forest();
        Forest undrawnCard = new Forest();
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.setLibrary(player1, List.of(casterDraw));
        harness.setLibrary(player2, List.of(targetDraw, undrawnCard, new Forest()));
        harness.setHand(player1, List.of(new DamnablePact()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(targetDraw);
        assertThat(gd.playerDecks.get(player2.getId())).contains(undrawnCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }
}
