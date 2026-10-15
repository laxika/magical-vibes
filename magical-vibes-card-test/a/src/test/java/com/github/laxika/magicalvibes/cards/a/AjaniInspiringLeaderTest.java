package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AjaniInspiringLeader.class, GreenwoodSentinel.class, Mountain.class, SarkhanTheMasterless.class})
class AjaniInspiringLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("+2 gains 2 life and puts two +1/+1 counters on up to one target creature")
    void plusTwoGainsLifeAndPutsCountersOnTarget() {
        Permanent ajani = addReadyAjani(4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+2 can be activated without choosing a creature")
    void plusTwoCanChooseNoCreature() {
        Permanent ajani = addReadyAjani(4);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 exiles a creature and its controller gains 2 life")
    void minusThreeExilesCreatureAndItsControllerGainsLife() {
        Permanent ajani = addReadyAjani(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 cannot target a noncreature permanent")
    void minusThreeCannotTargetNoncreature() {
        Permanent ajani = addReadyAjani(4);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-10 grants flying and double strike to your creatures until end of turn")
    void minusTenGrantsKeywordsUntilEndOfTurn() {
        Permanent ajani = addReadyAjani(10);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void plusTwoGainsNoLifeWhenChosenTargetLeavesBeforeResolution() {
        Permanent ajani = addReadyAjani(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void minusThreeCanExileYourOwnCreatureAndGiveYouLife() {
        addReadyAjani(4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 10);
    }

    @Test
    void minusTenDoesNotAffectCreaturesEnteringAfterResolution() {
        addReadyAjani(10);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @CardUsed({AjaniInspiringLeader.class, SarkhanTheMasterless.class})
    void minusTenIncludesAjaniWhenHeIsACreature() {
        Permanent ajani = addReadyAjani(11);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ajani, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ajani, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private Permanent addReadyAjani(int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new AjaniInspiringLeader());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
