package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntruderAlarm.class, YouthfulKnight.class})
class IntruderAlarmTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creatures do not untap during their controller's untap step")
    void creaturesStayTappedThroughUntapStep() {
        Permanent alarm = harness.addToBattlefieldAndReturn(player1, new IntruderAlarm());
        Permanent ownCreature = addCreatureReady(player1, new YouthfulKnight());
        Permanent opponentCreature = addCreatureReady(player2, new YouthfulKnight());
        ownCreature.tap();
        opponentCreature.tap();
        alarm.tap();

        harness.performUntapStep(player1);
        harness.performUntapStep(player2);

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(alarm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature entering untaps all creatures")
    void creatureEnteringUntapsAllCreatures() {
        harness.addToBattlefield(player1, new IntruderAlarm());
        Permanent ownCreature = addCreatureReady(player1, new YouthfulKnight());
        Permanent opponentCreature = addCreatureReady(player2, new YouthfulKnight());
        ownCreature.tap();
        opponentCreature.tap();

        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");
        resolveAllTriggers();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature entering untaps all creatures")
    void opponentCreatureEnteringUntapsAllCreatures() {
        harness.addToBattlefield(player1, new IntruderAlarm());
        Permanent ownCreature = addCreatureReady(player1, new YouthfulKnight());
        ownCreature.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new YouthfulKnight(), "{1}{W}");
        resolveAllTriggers();

        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A noncreature entering does not trigger the untap ability")
    void noncreatureEnteringDoesNotUntapCreatures() {
        harness.addToBattlefield(player1, new IntruderAlarm());
        Permanent creature = addCreatureReady(player1, new YouthfulKnight());
        creature.tap();

        harness.enterBattlefieldAndReturn(player1, new IntruderAlarm());

        assertThat(creature.isTapped()).isTrue();
    }
}
