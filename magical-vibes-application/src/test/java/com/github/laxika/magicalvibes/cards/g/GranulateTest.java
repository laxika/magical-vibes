package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EonHub;
import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.cards.m.MyrQuadropod;
import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Granulate.class, EonHub.class, FurnaceWhelp.class, MyrQuadropod.class,
        MyrServitor.class, SeatOfTheSynod.class})
class GranulateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys each nonland artifact with mana value 4 or less")
    void destroysMatchingArtifactsAcrossBothBattlefields() {
        harness.addToBattlefield(player1, new MyrServitor());
        harness.addToBattlefield(player1, new MyrQuadropod());
        harness.addToBattlefield(player1, new EonHub());
        harness.addToBattlefield(player1, new FurnaceWhelp());
        harness.addToBattlefield(player1, new SeatOfTheSynod());
        harness.addToBattlefield(player2, new MyrServitor());

        harness.setHand(player1, List.of(new Granulate()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Myr Servitor");
        harness.assertNotOnBattlefield(player2, "Myr Servitor");
        harness.assertNotOnBattlefield(player1, "Myr Quadropod");
        harness.assertOnBattlefield(player1, "Eon Hub");
        harness.assertOnBattlefield(player1, "Furnace Whelp");
        harness.assertOnBattlefield(player1, "Seat of the Synod");
    }
}
