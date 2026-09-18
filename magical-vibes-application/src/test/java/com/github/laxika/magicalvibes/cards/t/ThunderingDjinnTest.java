package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderingDjinn.class, Forest.class, GrizzlyBears.class})
class ThunderingDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals damage equal to cards drawn this turn to a player")
    void attackingDealsDamageEqualToCardsDrawnThisTurnToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ThunderingDjinn());
        drawCardsThisTurn(2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Attacking deals damage equal to cards drawn this turn to a creature")
    void attackingDealsDamageEqualToCardsDrawnThisTurnToCreature() {
        addCreatureReady(player1, new ThunderingDjinn());
        harness.addToBattlefield(player2, new GrizzlyBears());
        drawCardsThisTurn(2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void drawCardsThisTurn(int count) {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.inMutationScope(() -> {
            for (int i = 0; i < count; i++) {
                harness.getDrawService().resolveDrawCard(gd, player1.getId());
            }
        });
    }
}
