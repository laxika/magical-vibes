package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepSeaTerror.class, FieryImpulse.class})
class DeepSeaTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack with exactly seven cards in its controller's graveyard")
    void canAttackWithSevenCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, graveyardCards(7));

        Permanent terror = addCreatureReady(player1, new DeepSeaTerror());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(terror)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can attack with more than seven cards in graveyard")
    void canAttackWithMoreThanSevenCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, graveyardCards(9));

        Permanent terror = addCreatureReady(player1, new DeepSeaTerror());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(terror)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot attack with six cards in graveyard")
    void cannotAttackWithSixCards() {
        harness.setGraveyard(player1, graveyardCards(6));

        Permanent terror = addCreatureReady(player1, new DeepSeaTerror());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(terror);
        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack with an empty graveyard")
    void cannotAttackWithEmptyGraveyard() {
        Permanent terror = addCreatureReady(player1, new DeepSeaTerror());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(terror);
        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the controller's own graveyard counts")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(10));

        Permanent terror = addCreatureReady(player1, new DeepSeaTerror());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(terror);
        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new FieryImpulse());
        }
        return cards;
    }

    @Test
    @DisplayName("Creature and noncreature cards both count toward the threshold")
    void allCardTypesCount() {
        List<Card> cards = graveyardCards(6);
        cards.add(new DeepSeaTerror());
        harness.setGraveyard(player1, cards);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DeepSeaTerror());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can block with an empty graveyard")
    void canBlockWithEmptyGraveyard() {
        harness.setGraveyard(player1, graveyardCards(7));
        addCreatureReady(player1, new DeepSeaTerror());
        Permanent blocker = addCreatureReady(player2, new DeepSeaTerror());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }
}