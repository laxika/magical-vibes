package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvigoratingFalls.class, AvenTrooper.class})
class InvigoratingFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life for each creature card in all graveyards")
    void gainsLifeForCreatureCardsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new AvenTrooper(), new AvenTrooper(), new InvigoratingFalls()));
        harness.setGraveyard(player2, List.of(new AvenTrooper(), new InvigoratingFalls()));

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new InvigoratingFalls(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Gains no life when all graveyards lack creature cards")
    void gainsNoLifeWithoutCreatureCards() {
        harness.setGraveyard(player1, List.of(new InvigoratingFalls()));
        harness.setGraveyard(player2, List.of(new InvigoratingFalls()));

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new InvigoratingFalls(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Counts creature cards added to graveyards before resolution")
    void countsCreaturesAddedBeforeResolution() {
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new InvigoratingFalls(), "{2}{G}{G}");
        harness.setGraveyard(player1, List.of(new AvenTrooper()));
        harness.setGraveyard(player2, List.of(new AvenTrooper(), new AvenTrooper()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Does not count creature cards removed from graveyards before resolution")
    void doesNotCountCreaturesRemovedBeforeResolution() {
        harness.setGraveyard(player1, List.of(new AvenTrooper()));
        harness.setGraveyard(player2, List.of(new AvenTrooper(), new AvenTrooper()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new InvigoratingFalls(), "{2}{G}{G}");
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new AvenTrooper()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }
}
