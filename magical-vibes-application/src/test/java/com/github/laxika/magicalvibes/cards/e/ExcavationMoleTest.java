package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExcavationMole.class, Forest.class})
class ExcavationMoleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from its controller's library")
    void etbMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB mills its controller's library, not the opponent's")
    void etbMillsControllerOnly() {
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        castAndResolve();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new ExcavationMole()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
