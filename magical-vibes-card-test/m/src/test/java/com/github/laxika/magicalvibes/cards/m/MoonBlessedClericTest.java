package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonBlessedCleric.class, MinimusContainment.class})
class MoonBlessedClericTest extends BaseCardTest {

    @Test
    void acceptingMayAbilityOffersOnlyEnchantmentsAndPutsTheChosenCardOnTop() {
        MinimusContainment enchantment = new MinimusContainment();
        MoonBlessedCleric creature = new MoonBlessedCleric();
        setLibrary(enchantment, creature);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(enchantment);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningMayAbilitySkipsTheSearch() {
        MinimusContainment enchantment = new MinimusContainment();
        setLibrary(enchantment);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
    }

    @Test
    void acceptingWithNoEnchantmentDoesNotCreateLibraryInteraction() {
        MoonBlessedCleric creature = new MoonBlessedCleric();
        setLibrary(creature);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void canFailToFindEvenWhenAnEnchantmentIsAvailable() {
        MinimusContainment enchantment = new MinimusContainment();
        MoonBlessedCleric creature = new MoonBlessedCleric();
        setLibrary(enchantment, creature);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(enchantment, creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptingWithAnEmptyLibraryCompletesTheAbility() {
        setLibrary();
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesOneOfMultipleEnchantmentsWithoutRemovingTheOthers() {
        MinimusContainment first = new MinimusContainment();
        MinimusContainment second = new MinimusContainment();
        MoonBlessedCleric creature = new MoonBlessedCleric();
        setLibrary(first, creature, second);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private void castMoonBlessedCleric() {
        harness.setHand(player1, List.of(new MoonBlessedCleric()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
