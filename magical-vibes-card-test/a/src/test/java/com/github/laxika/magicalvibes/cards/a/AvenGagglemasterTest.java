package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AvenGagglemaster.class, GrizzlyBears.class, SerraAngel.class, SnappingDrake.class, Unsummon.class})
class AvenGagglemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each flying creature you control")
    void gainsTwoLifePerControlledFlyingCreature() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new SnappingDrake());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new AvenGagglemaster(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Counts itself but not non-flying or opposing creatures")
    void countsOnlyControlledFlyingCreaturesIncludingItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new AvenGagglemaster(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains no life when its only flying creature leaves before the trigger resolves")
    void gainsNoLifeAfterSourceLeaves() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new AvenGagglemaster(), "{3}{W}{W}");
        harness.passBothPriorities();
        var source = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts flying creatures that enter after the trigger is put on the stack")
    void countsFlyingCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new AvenGagglemaster(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new AvenGagglemaster());
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.passBothPriorities();

        harness.assertLife(player1, 28);
    }
}
