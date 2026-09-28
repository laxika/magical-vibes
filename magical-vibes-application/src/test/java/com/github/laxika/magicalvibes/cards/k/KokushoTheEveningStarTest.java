package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.p.PullUnder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KokushoTheEveningStar.class, PullUnder.class})
class KokushoTheEveningStarTest extends BaseCardTest {

    @Test
    @DisplayName("On death, each opponent loses 5 life and the controller gains that much")
    void deathTriggerDrainsOpponent() {
        Permanent kokusho = addCreatureReady(player1, new KokushoTheEveningStar());

        int p1Before = gd.getLife(player1.getId());
        int p2Before = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PullUnder()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player2, 0, kokusho.getId());
        harness.passBothPriorities(); // Pull Under resolves — Kokusho dies
        harness.passBothPriorities(); // death trigger resolves

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2Before - 5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1Before + 5);
    }
}
