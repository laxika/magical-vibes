package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NivMizzetParunTest extends BaseCardTest {

    @Test
    @DisplayName("An instant cast by any player draws a card and the draw deals 1 damage")
    void instantCastDrawsAndDealsDamage() {
        addReadyNiv(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FugitiveWizard()));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fugitive Wizard");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A creature spell does not trigger the card draw ability")
    void creatureCastDoesNotDraw() {
        addReadyNiv(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Niv-Mizzet, Parun cannot be countered")
    void cannotBeCountered() {
        NivMizzetParun niv = new NivMizzetParun();
        harness.setHand(player1, List.of(niv));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, niv.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Niv-Mizzet, Parun");
        harness.assertInGraveyard(player2, "Cancel");
    }

    private Permanent addReadyNiv(Player player) {
        return addCreatureReady(player, new NivMizzetParun());
    }
}
