package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SoulShatter.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class})
class SoulShatterTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices an eligible permanent with greatest mana value")
    void sacrificesGreatestManaValueCreatureOrPlaneswalker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        addReadyChandra(player2, 5);

        castSoulShatter();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
    }

    private void castSoulShatter() {
        harness.setHand(player1, List.of(new SoulShatter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ChandraNalaar());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
