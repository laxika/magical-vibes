package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.r.RiverOfTears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GravePeril.class, FomoriNomad.class, MassOfGhouls.class, RiverOfTears.class})
class GravePerilTest extends BaseCardTest {

    @Test
    @DisplayName("A nonblack creature entering sacrifices Grave Peril and is destroyed")
    void nonblackCreatureEntrySacrificesAndDestroys() {
        harness.addToBattlefield(player1, new GravePeril());

        harness.castFromHand(player1, new FomoriNomad(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grave Peril");
        harness.assertInGraveyard(player1, "Fomori Nomad");
    }

    @Test
    @DisplayName("A black creature entering does not trigger Grave Peril")
    void blackCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GravePeril());

        harness.castFromHand(player1, new MassOfGhouls(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grave Peril");
        harness.assertOnBattlefield(player1, "Mass of Ghouls");
    }

    @Test
    @DisplayName("A nonblack creature entering under an opponent's control is destroyed")
    void opponentNonblackCreatureEntryIsDestroyed() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new FomoriNomad(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grave Peril");
        harness.assertInGraveyard(player2, "Fomori Nomad");
    }

    @Test
    @DisplayName("A noncreature permanent entering does not trigger Grave Peril")
    void noncreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RiverOfTears()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Grave Peril");
        harness.assertOnBattlefield(player1, "River of Tears");
    }

    @Test
    @DisplayName("If Grave Peril leaves before its trigger resolves, the entering creature survives")
    void sourceLeavingBeforeResolutionPreventsDestruction() {
        harness.addToBattlefield(player1, new GravePeril());

        harness.castFromHand(player1, new FomoriNomad(), "{4}{R}");
        harness.passBothPriorities();
        var gravePeril = findPermanent(player1, "Grave Peril");
        gd.playerBattlefields.get(player1.getId()).remove(gravePeril);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fomori Nomad");
    }
}
