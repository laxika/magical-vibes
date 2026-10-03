package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AgentsOfSHIELD;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaLivingLegend.class, AgentsOfSHIELD.class})
class CaptainAmericaLivingLegendTest extends BaseCardTest {

    @Test
    void untapsEachCreatureTheFirstTimeItBecomesTappedDuringYourTurn() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent first = addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent second = addCreatureReady(player1, new AgentsOfSHIELD());

        tapAndCollect(first);
        tapAndCollect(second);
        resolveAllTriggers();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerAgainWhenTheSameCreatureBecomesTappedLaterThatTurn() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent creature = addCreatureReady(player1, new AgentsOfSHIELD());

        tapAndCollect(creature);
        resolveAllTriggers();
        tapAndCollect(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remembersATapFromBeforeCaptainEnteredTheBattlefield() {
        Permanent creature = addCreatureReady(player1, new AgentsOfSHIELD());

        tapAndCollect(creature);
        creature.untap();
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        tapAndCollect(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersOnlyDuringCaptainsControllersTurn() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent creature = addCreatureReady(player1, new AgentsOfSHIELD());
        harness.forceActivePlayer(player2);

        tapAndCollect(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void tapAndCollect(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, permanent));
    }

    @Test
    void untapsCaptainHimselfWhenHeBecomesTapped() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaLivingLegend());

        tapAndCollect(captain);
        resolveAllTriggers();

        assertThat(captain.isTapped()).isFalse();
    }

    @Test
    void doesNotUntapAnOpponentsCreatureDuringYourTurn() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent creature = addCreatureReady(player2, new AgentsOfSHIELD());

        tapAndCollect(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remembersATapUnderAnotherControllerEarlierInTheTurn() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent creature = addCreatureReady(player2, new AgentsOfSHIELD());
        tapAndCollect(creature);
        creature.untap();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        tapAndCollect(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstTapTriggerStillUntapsAfterAnUntapAndRetapInResponse() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent creature = addCreatureReady(player1, new AgentsOfSHIELD());
        tapAndCollect(creature);
        assertThat(gd.stack).hasSize(1);

        creature.untap();
        tapAndCollect(creature);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void abilityStillResolvesAfterCaptainLeavesTheBattlefield() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent creature = addCreatureReady(player1, new AgentsOfSHIELD());
        tapAndCollect(creature);
        gd.playerBattlefields.get(player1.getId()).remove(captain);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
    }

}
