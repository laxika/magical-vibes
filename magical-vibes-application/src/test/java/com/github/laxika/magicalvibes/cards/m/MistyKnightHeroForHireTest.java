package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WildGuess;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistyKnightHeroForHire.class, Forest.class, Mountain.class, WildGuess.class})
class MistyKnightHeroForHireTest extends BaseCardTest {

    @Test
    void paysCostsBeforeDrawingOneCardForItsOwnDiscard() {
        var misty = addCreatureReady(player1, new MistyKnightHeroForHire());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(misty.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotCountCardsDiscardedByOpponent() {
        addCreatureReady(player1, new MistyKnightHeroForHire());
        addCreatureReady(player2, new MistyKnightHeroForHire());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        var misty = addCreatureReady(player1, new MistyKnightHeroForHire());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(misty.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

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
