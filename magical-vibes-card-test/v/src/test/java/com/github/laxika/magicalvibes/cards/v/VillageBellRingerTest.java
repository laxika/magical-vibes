package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VillageBellRinger.class, WalkingCorpse.class, Island.class})
class VillageBellRingerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger puts triggered ability on the stack")
    void etbPutsTriggerOnStack() {
        harness.castFromHand(player1, new VillageBellRinger(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Untaps all tapped creatures you control when ETB resolves")
    void untapsAllTappedCreaturesYouControl() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        creature1.tap();
        creature2.tap();

        harness.castFromHand(player1, new VillageBellRinger(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(creature1.isTapped()).isFalse();
        assertThat(creature2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        opponentCreature.tap();

        harness.castFromHand(player1, new VillageBellRinger(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap non-creature permanents")
    void doesNotUntapNonCreaturePermanents() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        harness.castFromHand(player1, new VillageBellRinger(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Village Bell-Ringer enters the battlefield")
    void entersBattlefield() {
        harness.castFromHand(player1, new VillageBellRinger(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Village Bell-Ringer");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's turn and untapping your creatures")
    void canCastDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        creature.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new VillageBellRinger(), "{2}{W}");
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Village Bell-Ringer");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untaps itself and creatures added after the trigger was put on the stack")
    void untapsCreaturesPresentAtResolutionIncludingItself() {
        harness.castFromHand(player1, new VillageBellRinger(), "{2}{W}");
        harness.passBothPriorities();
        Permanent bellRinger = gd.playerBattlefields.get(player1.getId()).getFirst();
        bellRinger.tap();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        lateCreature.tap();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(bellRinger.isTapped()).isFalse();
        assertThat(lateCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
