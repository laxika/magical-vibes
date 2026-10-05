package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MireTriton.class, Forest.class})
class MireTritonTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void gainsFullLifeEvenWhenLibraryHasFewerThanTwoCards(int librarySize) {
        List<Forest> library = librarySize == 0 ? List.of() : List.of(new Forest());
        harness.setLibrary(player1, library);
        harness.setLife(player1, 10);

        harness.enterBattlefieldAndReturn(player1, new MireTriton());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertLife(player1, 12);
    }

    @Test
    void triggerWaitsForResolutionAndOnlyAffectsItsController() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest opponentCard = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.setLife(player2, 10);
        harness.setLife(player1, 15);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MireTriton()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mire Triton");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, third);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        harness.assertLife(player2, 12);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 15);
    }

    @Test
    void enteringBattlefieldMillsTwoCardsAndGainsTwoLife() {
        Forest firstMilledCard = new Forest();
        Forest secondMilledCard = new Forest();
        harness.setLibrary(player1, List.of(firstMilledCard, secondMilledCard));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new MireTriton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMilledCard, secondMilledCard);
        harness.assertLife(player1, 12);
    }
}
