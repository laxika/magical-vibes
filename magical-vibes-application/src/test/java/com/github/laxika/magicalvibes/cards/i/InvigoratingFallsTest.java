package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvigoratingFalls.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class InvigoratingFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life for each creature card in all graveyards")
    void gainsLifeForCreatureCardsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Mountain()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Mountain()));
        harness.setHand(player1, List.of(new InvigoratingFalls()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Gains no life when all graveyards lack creature cards")
    void gainsNoLifeWithoutCreatureCards() {
        harness.setGraveyard(player1, List.of(new Mountain()));
        harness.setGraveyard(player2, List.of(new Mountain()));
        harness.setHand(player1, List.of(new InvigoratingFalls()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
