package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GluttonousSlug.class, GrizzlyBears.class})
class GluttonousSlugTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Gluttonous Slug when a larger creature enters")
    void evolvesForLargerCreature() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger when neither stat is greater")
    void doesNotEvolveForEqualCreature() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());

        harness.setHand(player1, List.of(new GluttonousSlug()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
