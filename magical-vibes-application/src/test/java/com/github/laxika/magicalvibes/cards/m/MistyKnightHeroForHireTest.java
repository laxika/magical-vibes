package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WildGuess;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistyKnightHeroForHire.class, Forest.class, Mountain.class, WildGuess.class})
class MistyKnightHeroForHireTest extends BaseCardTest {

    @Test
    void drawsForEveryCardDiscardedThisTurnIncludingActivationCost() {
        harness.setHand(player1, List.of(new WildGuess(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        addCreatureReady(player1, new MistyKnightHeroForHire());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
