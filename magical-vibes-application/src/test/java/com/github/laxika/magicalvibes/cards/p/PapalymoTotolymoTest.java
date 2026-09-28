package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PapalymoTotolymo.class, GrizzlyBears.class, HillGiant.class, Opt.class})
class PapalymoTotolymoTest extends BaseCardTest {

    @Test
    void noncreatureSpellDealsDamageAndGainsLife() {
        addReadyPapalymo(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void activatedAbilitySacrificesOpponentsGreatestPowerCreatureAfterLifeLoss() {
        addReadyPapalymo(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void activatedAbilitySkipsOpponentsWhoDidNotLoseLife() {
        addReadyPapalymo(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    private Permanent addReadyPapalymo(Player player) {
        Permanent papalymo = new Permanent(new PapalymoTotolymo());
        papalymo.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(papalymo);
        return papalymo;
    }
}
