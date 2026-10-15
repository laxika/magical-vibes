package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptWithBunnies.class, Forest.class})
class TemptWithBunniesTest extends BaseCardTest {

    @Test
    void acceptingOpponentDrawsAndCreatesForBothPlayers() {
        castTemptWithBunnies();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Rabbit")).isEqualTo(2);
        assertThat(countPermanents(player2, "Rabbit")).isOne();
    }

    @Test
    void decliningOpponentLeavesOnlyTheMandatoryControllerDrawAndToken() {
        castTemptWithBunnies();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Rabbit")).isOne();
        assertThat(countPermanents(player2, "Rabbit")).isZero();
    }

    private void castTemptWithBunnies() {
        harness.setHand(player1, List.of(new TemptWithBunnies()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
