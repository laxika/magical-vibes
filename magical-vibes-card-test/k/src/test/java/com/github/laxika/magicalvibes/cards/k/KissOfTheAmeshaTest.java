package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
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

@CardUsed({KissOfTheAmesha.class, DruidOfTheAnima.class})
class KissOfTheAmeshaTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 7 life and draws two cards")
    void targetPlayerGainsLifeAndDraws() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castKissTargeting(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(27);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castKissTargeting(player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(27);
        // Kiss was the only card in hand; after casting it and resolving, only the two drawn cards remain.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castKissTargeting(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DruidOfTheAnima());

        harness.setHand(player1, List.of(new KissOfTheAmesha()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Drawing the last two cards does not cause a loss")
    void drawingExactlyTwoRemainingCardsDoesNotCauseLoss() {
        DruidOfTheAnima first = new DruidOfTheAnima();
        DruidOfTheAnima second = new DruidOfTheAnima();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(first, second));

        castKissTargeting(player2.getId());

        harness.assertLife(player2, 27);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Target gains life and draws the remaining card before losing for the second draw")
    void targetWithOneRemainingCardLosesAfterGainingLife() {
        DruidOfTheAnima remaining = new DruidOfTheAnima();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(remaining));

        castKissTargeting(player2.getId());

        harness.assertLife(player2, 27);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private void castKissTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new KissOfTheAmesha()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
