package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.q.Quagnoth;
import com.github.laxika.magicalvibes.cards.r.RiverOfTears;
import com.github.laxika.magicalvibes.cards.s.SliverLegion;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravePeril.class, FomoriNomad.class, MassOfGhouls.class, RiverOfTears.class,
        DryadArbor.class, Quagnoth.class, SliverLegion.class})
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
        var gravePeril = harness.addToBattlefieldAndReturn(player1, new GravePeril());

        harness.castFromHand(player1, new FomoriNomad(), "{4}{R}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(gravePeril);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fomori Nomad");
    }

    @Test
    @DisplayName("Only the most recent creature is destroyed when two entry triggers are pending")
    void onlyLastEntryIsDestroyed() {
        harness.addToBattlefield(player1, new GravePeril());
        var first = harness.enterBattlefieldAndReturn(player1, new FomoriNomad());
        var second = harness.enterBattlefieldAndReturn(player1, new FomoriNomad());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player1, "Grave Peril");
    }

    @Test
    @DisplayName("Grave Peril is still sacrificed when the entering creature has left")
    void creatureLeavingDoesNotPreventSacrifice() {
        harness.addToBattlefield(player1, new GravePeril());
        var creature = harness.enterBattlefieldAndReturn(player1, new FomoriNomad());
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grave Peril");
    }

    @Test
    @DisplayName("Shroud does not stop Grave Peril's destruction")
    void shroudDoesNotPreventDestruction() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.castFromHand(player1, new Quagnoth(), "{5}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grave Peril");
        harness.assertInGraveyard(player1, "Quagnoth");
    }

    @Test
    @DisplayName("A multicolored creature that is black does not trigger Grave Peril")
    void multicoloredBlackCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.castFromHand(player1, new SliverLegion(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grave Peril");
        harness.assertOnBattlefield(player1, "Sliver Legion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature land entering triggers Grave Peril")
    void creatureLandEntryTriggers() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.setHand(player1, List.of(new DryadArbor()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grave Peril");
        harness.assertInGraveyard(player1, "Dryad Arbor");
    }
}
