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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void canReplicateItselfWithoutAnotherHatcherySliverOnTheBattlefield() {
        harness.setHand(player1, List.of(new HatcherySliver()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}", "{1}{G}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(findPermanents(player1, "Hatchery Sliver")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Hatchery Sliver")).hasSize(3);
        assertThat(findPermanents(player1, "Hatchery Sliver").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    void canCastItselfWithoutPayingReplicate() {
        harness.setHand(player1, List.of(new HatcherySliver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Hatchery Sliver")).hasSize(1);
        assertThat(findPermanent(player1, "Hatchery Sliver").getCard().isToken()).isFalse();
    }

    @Test
    void createsOneTokenForEachGrantedReplicatePayment() {
        addCreatureReady(player1, new HatcherySliver());
        harness.setHand(player1, List.of(new MetallicSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}", "{1}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(findPermanents(player1, "Metallic Sliver")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Metallic Sliver")).hasSize(3);
        assertThat(findPermanents(player1, "Metallic Sliver").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    void doesNotGiveReplicateToAnOpponentsSliverSpell() {
        addCreatureReady(player2, new HatcherySliver());
        harness.setHand(player1, List.of(new MetallicSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has no repeatable additional cost");

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Metallic Sliver")).hasSize(1);
        assertThat(findPermanent(player1, "Metallic Sliver").getCard().isToken()).isFalse();
    }
}
