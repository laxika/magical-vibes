package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarduHeartPiercerTest extends BaseCardTest {

    @Test
    void etbDeals2DamageToCreatureWithRaid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castMarduHeartPiercer();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void etbDeals2DamageToPlayerWithRaid() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castMarduHeartPiercer();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void etbDoesNotTriggerWithoutRaid() {
        harness.setLife(player2, 20);
        castMarduHeartPiercer();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Mardu Heart-Piercer");
    }

    @Test
    void etbDoesNothingIfRaidIsLostBeforeResolution() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castMarduHeartPiercer();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playersDeclaredAttackersThisTurn.clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castMarduHeartPiercer() {
        harness.setHand(player1, List.of(new MarduHeartPiercer()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }
}
