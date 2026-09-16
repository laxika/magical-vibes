package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KraumLudevicsOpus.class, Shock.class})
class KraumLudevicsOpusTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent casts their second spell each turn")
    void drawsCardForOpponentsSecondSpell() {
        harness.addToBattlefield(player1, new KraumLudevicsOpus());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
