package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Halfdane.class, GiantSpider.class})
class HalfdaneTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger targets another creature and copies its current power and toughness")
    void targetsAnotherCreatureAndCopiesCurrentStats() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(spider.getId())
                .doesNotContain(halfdane.getId());

        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(4);

        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(4);
    }

    @Test
    @DisplayName("Changed base power and toughness last through the end of the next upkeep")
    void lastsThroughEndOfNextUpkeep() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spider));
        advanceToUpkeep(player1);

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(4);

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(3);
    }
}
