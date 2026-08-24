package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavnicaAtWar.class, QasaliAmbusher.class, GrizzlyBears.class, MindStone.class, Forest.class})
class RavnicaAtWarTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all multicolored permanents and leaves other permanents")
    void exilesAllMulticoloredPermanents() {
        harness.addToBattlefield(player1, new QasaliAmbusher());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new QasaliAmbusher());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RavnicaAtWar()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Qasali Ambusher");
        harness.assertNotOnBattlefield(player2, "Qasali Ambusher");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Qasali Ambusher");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Qasali Ambusher");
    }
}
