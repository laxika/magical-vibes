package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({FireMagic.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class FireMagicTest extends BaseCardTest {

    @Test
    void fireDealsOneDamageToEachCreature() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(0, 0);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void firaDealsTwoDamageToEachCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        cast(1, 2);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void firagaDealsThreeDamageToEachCreature() {
        harness.addToBattlefield(player2, new HillGiant());

        cast(2, 5);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    private void cast(int mode, int colorlessMana) {
        harness.setHand(player1, List.of(new FireMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }
}
