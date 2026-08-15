package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.t.TreeOfTales;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DaringThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the inspired ability exchanges control of matching permanents")
    void exchangesControlWhenAccepted() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new DaringThief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        thief.tap();

        advanceToInspiredTrigger();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent);
    }

    @Test
    @DisplayName("Declining the inspired ability leaves both permanents under their original control")
    void declineLeavesControlUnchanged() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new DaringThief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        thief.tap();

        advanceToInspiredTrigger();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
    }

    @Test
    @DisplayName("A pair that shares no card type cannot be chosen")
    void nonmatchingPairCannotBeChosen() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new DaringThief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addToBattlefieldAndReturn(player2, new Millstone());
        thief.tap();

        advanceToInspiredTrigger();
        harness.handlePermanentChosen(player1, own.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
    }

    @Test
    @DisplayName("A land that shares another card type can be the second target")
    void landWithSharedCardTypeCanBeSecondTarget() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new DaringThief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new TreeOfTales());
        thief.tap();

        advanceToInspiredTrigger();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent);
    }

    private void advanceToInspiredTrigger() {
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
