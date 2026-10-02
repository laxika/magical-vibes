package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbushViper.class, FortressCrab.class})
class AmbushViperTest extends BaseCardTest {

    @Test
    void canCastAndResolveDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new AmbushViper(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ambush Viper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new AmbushViper(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ambush Viper");
    }

    @Test
    void deathtouchDestroysHighToughnessBlockerEvenWhenViperDies() {
        addCreatureReady(player1, new AmbushViper());
        harness.addToBattlefield(player2, new FortressCrab());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, Map.of(0, List.of(0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Ambush Viper");
        harness.assertInGraveyard(player2, "Fortress Crab");
        harness.assertNotOnBattlefield(player2, "Fortress Crab");
    }

    @Test
    void deathtouchDestroysHighToughnessAttackerWhenBlocking() {
        addCreatureReady(player1, new FortressCrab());
        harness.addToBattlefield(player2, new AmbushViper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, Map.of(0, List.of(0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Fortress Crab");
        harness.assertInGraveyard(player2, "Ambush Viper");
        harness.assertNotOnBattlefield(player1, "Fortress Crab");
    }
}
