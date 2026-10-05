package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IndathaCrystal;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KerugaTheMacrosage.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        SerraAngel.class, IndathaCrystal.class, Unsummon.class})
class KerugaTheMacrosageTest extends BaseCardTest {

    @Test
    void drawsForEachOtherControlledPermanentWithManaValueAtLeastThree() {
        harness.setHand(player1, List.of(new KerugaTheMacrosage()));
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SerraAngel());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        harness.assertOnBattlefield(player1, "Keruga, the Macrosage");
    }

    @Test
    void excludesKerugaAndOpponentPermanentsFromTheCount() {
        harness.setHand(player1, List.of(new KerugaTheMacrosage()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SerraAngel());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    void countsNoncreaturePermanentsAtExactlyThreeManaValue() {
        harness.setHand(player1, List.of(new KerugaTheMacrosage()));
        harness.addToBattlefield(player1, new IndathaCrystal());
        harness.addToBattlefield(player2, new IndathaCrystal());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void countsPermanentsWhenTheTriggerResolves() {
        harness.setHand(player1, List.of(new KerugaTheMacrosage(), new Unsummon()));
        var giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new IndathaCrystal());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInHand(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void triggerStillDrawsAfterKerugaLeavesTheBattlefield() {
        harness.setHand(player1, List.of(new KerugaTheMacrosage(), new Unsummon()));
        harness.addToBattlefield(player1, new IndathaCrystal());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Keruga, the Macrosage"));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInHand(player1, "Keruga, the Macrosage");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
