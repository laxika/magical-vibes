package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HatcherySliver.class, MetallicSliver.class, GrizzlyBears.class})
class HatcherySliverTest extends BaseCardTest {

    @Test
    void givesSliverSpellsReplicateAtTheirManaCost() {
        addCreatureReady(player1, new HatcherySliver());
        harness.setHand(player1, List.of(new MetallicSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Metallic Sliver")).hasSize(2);
        assertThat(findPermanents(player1, "Metallic Sliver"))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void doesNotGiveReplicateToNonSliverSpells() {
        addCreatureReady(player1, new HatcherySliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }
}
