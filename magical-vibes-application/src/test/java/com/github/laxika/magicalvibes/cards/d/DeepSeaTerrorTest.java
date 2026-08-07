package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepSeaTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack with exactly seven cards in its controller's graveyard")
    void canAttackWithSevenCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, graveyardCards(7));

        Permanent terror = readyTerror();

        declareAttack(terror);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can attack with more than seven cards in graveyard")
    void canAttackWithMoreThanSevenCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, graveyardCards(9));

        Permanent terror = readyTerror();

        declareAttack(terror);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot attack with six cards in graveyard")
    void cannotAttackWithSixCards() {
        harness.setGraveyard(player1, graveyardCards(6));

        Permanent terror = readyTerror();
        prepareCombat();

        int index = findIndex(terror);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack with an empty graveyard")
    void cannotAttackWithEmptyGraveyard() {
        Permanent terror = readyTerror();
        prepareCombat();

        int index = findIndex(terror);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the controller's own graveyard counts")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(10));

        Permanent terror = readyTerror();
        prepareCombat();

        int index = findIndex(terror);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    private Permanent readyTerror() {
        Permanent terror = new Permanent(new DeepSeaTerror());
        terror.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(terror);
        return terror;
    }

    private void prepareCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareAttack(Permanent terror) {
        prepareCombat();
        gs.declareAttackers(gd, player1, List.of(findIndex(terror)));
    }

    private int findIndex(Permanent target) {
        Player player = player1;
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i) == target) return i;
        }
        throw new IllegalStateException("Permanent not found on battlefield");
    }
}
