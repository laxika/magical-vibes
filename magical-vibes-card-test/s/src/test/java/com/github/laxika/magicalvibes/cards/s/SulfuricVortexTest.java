package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.cards.w.WipeClean;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SulfuricVortex.class, RenewedFaith.class, WipeClean.class})
class SulfuricVortexTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToActivePlayerOnControllerUpkeep() {
        harness.addToBattlefield(player1, new SulfuricVortex());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsTwoDamageToActivePlayerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new SulfuricVortex());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void preventsControllerLifeGain() {
        harness.addToBattlefield(player1, new SulfuricVortex());
        harness.castFromHand(player1, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void preventsOpponentLifeGain() {
        harness.addToBattlefield(player1, new SulfuricVortex());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void upkeepTriggerStillDealsDamageAfterVortexIsExiled() {
        harness.addToBattlefield(player1, new SulfuricVortex());
        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new WipeClean()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Sulfuric Vortex"));
        harness.assertNotOnBattlefield(player1, "Sulfuric Vortex");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);

        harness.castFromHand(player2, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();
        harness.assertLife(player2, 24);
    }

    @Test
    void multipleVorticesEachDealDamageOnUpkeep() {
        harness.addToBattlefield(player1, new SulfuricVortex());
        harness.addToBattlefield(player2, new SulfuricVortex());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }
}
