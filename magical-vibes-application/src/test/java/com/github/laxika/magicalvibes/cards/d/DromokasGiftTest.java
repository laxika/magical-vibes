package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromokasGift.class, GiantSpider.class, GrizzlyBears.class})
@DisplayName("Dromoka's Gift")
class DromokasGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Puts four +1/+1 counters on the creature with the least toughness")
    void bolstersLeastToughCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        castDromokasGift();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lets the controller choose among creatures tied for least toughness")
    void choosesAmongLeastToughnessCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        castDromokasGift();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 4));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does nothing when the controller has no creatures")
    void doesNothingWithoutCreatures() {
        castDromokasGift();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castDromokasGift() {
        harness.setHand(player1, List.of(new DromokasGift()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
