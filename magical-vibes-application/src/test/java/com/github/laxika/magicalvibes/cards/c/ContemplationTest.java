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

    @Test
    void castingContemplationDoesNotTriggerItsOwnAbility() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new Contemplation(), "{1}{W}{W}");

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Contemplation")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void noncreatureSpellTriggersLifeGainBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new Contemplation());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new Contemplation(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Contemplation")).isEqualTo(1);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Contemplation")).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void eachControlledContemplationTriggersSeparately() {
        harness.addToBattlefield(player1, new Contemplation());
        harness.addToBattlefield(player1, new Contemplation());
        harness.addToBattlefield(player2, new Contemplation());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void lifeGainTriggerResolvesAfterContemplationLeavesTheBattlefield() {
        var contemplation = harness.addToBattlefieldAndReturn(player1, new Contemplation());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");
        gd.playerBattlefields.get(player1.getId()).remove(contemplation);
        gd.playerGraveyards.get(player1.getId()).add(contemplation.getCard());

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }
}
