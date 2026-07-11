package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianGargantuaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the controller draw two cards and lose 2 life")
    void etbDrawsTwoAndLosesTwoLife() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        gd.playerDecks.get(player1.getId()).add(new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castGargantua(); // setHand leaves the hand empty after this spell is cast
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve draw
        harness.passBothPriorities(); // resolve life loss

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent is unaffected by the ETB trigger")
    void opponentUnaffected() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        gd.playerDecks.get(player1.getId()).add(new Forest());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castGargantua();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve draw
        harness.passBothPriorities(); // resolve life loss

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(opponentHandBefore);
    }

    private void castGargantua() {
        harness.setHand(player1, List.of(new PhyrexianGargantua()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
    }
}
