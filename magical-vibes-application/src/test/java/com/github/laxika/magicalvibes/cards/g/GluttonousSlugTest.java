package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GluttonousSlug.class, MotherBear.class, WallOfBlossoms.class})
class GluttonousSlugTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Gluttonous Slug when a larger creature enters")
    void evolvesForLargerCreature() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());

        harness.castFromHand(player1, new MotherBear(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger when neither stat is greater")
    void doesNotEvolveForEqualCreature() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());

        harness.castFromHand(player1, new GluttonousSlug(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve triggers for greater toughness even when power is equal")
    void evolvesForGreaterToughnessOnly() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());
        harness.setLibrary(player1, List.of(new MotherBear()));

        harness.castFromHand(player1, new WallOfBlossoms(), "{1}{G}");
        resolveAllTriggers();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's larger creature does not trigger evolve")
    void doesNotEvolveForOpponentsCreature() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new MotherBear(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve rechecks the comparison when the trigger resolves")
    void doesNotEvolveIfSourceHasGrownBeforeResolution() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());
        harness.castFromHand(player1, new MotherBear(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        slug.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve uses the entering creature's current stats at resolution")
    void doesNotEvolveIfEnteringCreatureHasShrunk() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GluttonousSlug());
        slug.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.castFromHand(player1, new MotherBear(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        findPermanent(player1, "Mother Bear").setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(slug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace prevents a single creature from blocking Gluttonous Slug")
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new GluttonousSlug());
        addCreatureReady(player2, new MotherBear());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two creatures to block Gluttonous Slug")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new GluttonousSlug());
        Permanent firstBlocker = addCreatureReady(player2, new MotherBear());
        Permanent secondBlocker = addCreatureReady(player2, new MotherBear());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
