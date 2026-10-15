package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoblinGlider;
import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Inspiration.class, GoblinGlider.class, AlmsCollector.class})
class InspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent draws two cards")
    void targetOpponentDrawsTwo() {
        harness.setHand(player1, List.of(new Inspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Inspiration");
    }

    @Test
    @DisplayName("Caster can target themselves to draw two cards")
    void targetSelfDrawsTwo() {
        harness.setHand(player1, List.of(new Inspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        // Started with one card (Inspiration), drew two, cast one.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GoblinGlider());
        harness.setHand(player1, List.of(new Inspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID creatureId = creature.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawingLastTwoCardsDoesNotLoseTheGame() {
        Inspiration first = new Inspiration();
        Inspiration second = new Inspiration();
        harness.setHand(player1, List.of(new Inspiration()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void targetLosesWhenOnlyOneCardRemains() {
        Inspiration remaining = new Inspiration();
        harness.setHand(player1, List.of(new Inspiration()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(remaining));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void almsCollectorReplacesTheTwoCardDrawInstruction() {
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.setHand(player1, List.of(new Inspiration()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Inspiration(), new Inspiration()));
        harness.setLibrary(player2, List.of(new Inspiration(), new Inspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
