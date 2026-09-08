package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResistanceSquad.class, EliteVanguard.class, GrizzlyBears.class})
class ResistanceSquadTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card when you control another Human")
    void etbDrawsWithAnotherHuman() {
        harness.addToBattlefield(player1, new EliteVanguard());
        int handBefore = castResistanceSquad();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("ETB does not draw when Resistance Squad is your only Human")
    void etbDoesNotDrawWithoutAnotherHuman() {
        int handBefore = castResistanceSquad();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB does not draw for an opponent's Human")
    void etbIgnoresOpponentsHuman() {
        harness.addToBattlefield(player2, new EliteVanguard());
        int handBefore = castResistanceSquad();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private int castResistanceSquad() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ResistanceSquad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        return gd.playerHands.get(player1.getId()).size();
    }
}
