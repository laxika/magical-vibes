package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DawnhartMentor;
import com.github.laxika.magicalvibes.cards.f.FestivalCrasher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CandlelitCavalry.class, GrizzlyBears.class, HillGiant.class, DawnhartMentor.class, FestivalCrasher.class})
class CandlelitCavalryTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }

    private void endTurn() {
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Coven grants trample when you control three creatures with different powers")
    void grantsTrampleWithCoven() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant trample when your creatures do not have three different powers")
    void doesNotGrantTrampleWithoutCoven() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Coven is checked again when the combat trigger resolves")
    void doesNotGrantTrampleIfCovenIsLostBeforeResolution() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new DawnhartMentor());
        Permanent crasher = harness.addToBattlefieldAndReturn(player1, new FestivalCrasher());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(crasher);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gaining Coven after combat begins does not create a trigger")
    void gainingCovenAfterCombatBeginsDoesNotGrantTrample() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new DawnhartMentor());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new DawnhartMentor());
        harness.addToBattlefield(player1, new FestivalCrasher());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opposing creatures do not count toward Coven")
    void doesNotCountOpposingCreatures() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new DawnhartMentor());
        harness.addToBattlefield(player2, new FestivalCrasher());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Duplicate powers do not prevent Coven and only Cavalry gains trample")
    void grantsOnlySelfTrampleDespiteDuplicatePowers() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new DawnhartMentor());
        Permanent crasher = harness.addToBattlefieldAndReturn(player1, new FestivalCrasher());
        harness.addToBattlefield(player1, new FestivalCrasher());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mentor, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, crasher, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(mentor);

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isTrue();
    }
}
