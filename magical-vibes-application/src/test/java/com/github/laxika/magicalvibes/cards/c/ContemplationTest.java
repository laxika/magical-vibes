package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Contemplation.class, YouthfulKnight.class})
class ContemplationTest extends BaseCardTest {

    @Test
    void controllerGainsLifeWhenCastingASpell() {
        harness.addToBattlefield(player1, new Contemplation());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // The life gain is a triggered ability and does not happen until it resolves.
        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void eachCastTriggersLifeGain() {
        harness.addToBattlefield(player1, new Contemplation());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");
        resolveAllTriggers();

        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");
        resolveAllTriggers();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void opponentCastingASpellDoesNotTriggerLifeGain() {
        harness.addToBattlefield(player1, new Contemplation());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new YouthfulKnight(), "{1}{W}");

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
