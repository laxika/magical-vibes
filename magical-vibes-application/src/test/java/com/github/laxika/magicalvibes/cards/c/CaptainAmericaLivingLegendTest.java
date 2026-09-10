package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaLivingLegend.class, GrizzlyBears.class})
class CaptainAmericaLivingLegendTest extends BaseCardTest {

    @Test
    void untapsEachCreatureTheFirstTimeItBecomesTappedDuringYourTurn() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        tapAndCollect(first);
        tapAndCollect(second);
        resolveAllTriggersDirectly();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerAgainWhenTheSameCreatureBecomesTappedLaterThatTurn() {
        addCreatureReady(player1, new CaptainAmericaLivingLegend());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        tapAndCollect(creature);
        resolveAllTriggersDirectly();
        tapAndCollect(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remembersATapFromBeforeCaptainEnteredTheBattlefield() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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

    private void resolveAllTriggersDirectly() {
        while (!gd.stack.isEmpty()) {
            harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        }
    }
}
