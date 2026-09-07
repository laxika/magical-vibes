package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cached Defenses")
class CachedDefensesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts three +1/+1 counters on the creature with the least toughness")
    void bolstersLeastToughCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        castCachedDefenses();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lets the controller choose among creatures tied for least toughness")
    void choosesAmongLeastToughnessCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        castCachedDefenses();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 3));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does nothing when the controller has no creatures")
    void doesNothingWithoutCreatures() {
        castCachedDefenses();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCachedDefenses() {
        harness.setHand(player1, List.of(new CachedDefenses()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
