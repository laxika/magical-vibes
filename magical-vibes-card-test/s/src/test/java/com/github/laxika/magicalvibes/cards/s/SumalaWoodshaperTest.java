package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.l.LuminousBonds;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RighteousBlow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SumalaWoodshaper.class, HitchclawRecluse.class, LuminousBonds.class,
        RighteousBlow.class, Plains.class})
class SumalaWoodshaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a creature or enchantment from the top four cards")
    void etbOffersCreatureOrEnchantment() {
        Card creature = new HitchclawRecluse();
        Card enchantment = new LuminousBonds();
        harness.setLibrary(player1, List.of(creature, enchantment, new RighteousBlow(), new Plains()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing an eligible card puts it into hand and bottoms the rest")
    void choosingEligibleCardPutsItIntoHand() {
        Card creature = new HitchclawRecluse();
        harness.setLibrary(player1, List.of(creature, new RighteousBlow(), new Plains(), new RighteousBlow()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining leaves all looked-at cards on the bottom")
    void decliningBottomsAllLookedAtCards() {
        Card instant1 = new RighteousBlow();
        Card enchantment = new LuminousBonds();
        Card plains = new Plains();
        Card instant2 = new RighteousBlow();
        harness.setLibrary(player1, List.of(instant1, enchantment, plains, instant2));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                instant1, enchantment, plains, instant2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosingEnchantmentRevealsItAndLeavesDeeperCardsOnTop() {
        Card enchantment = new LuminousBonds();
        Card creature = new HitchclawRecluse();
        Card instant = new RighteousBlow();
        Card land = new Plains();
        Card deeper = new HitchclawRecluse();
        harness.setLibrary(player1, List.of(enchantment, creature, instant, land, deeper));
        castAndResolveEtb();

        assertThat(gd.gameLog).noneMatch(entry ->
                entry.plainText().contains("Luminous Bonds"));
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.getFirst()).isSameAs(deeper);
        assertThat(library.subList(1, 4)).containsExactlyInAnyOrder(creature, instant, land);
        assertThat(gd.gameLog).anyMatch(entry ->
                entry.plainText().contains("Luminous Bonds") && entry.plainText().contains("into their hand"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryStillAllowsDecliningTheOnlyEligibleCard() {
        Card creature = new HitchclawRecluse();
        harness.setLibrary(player1, List.of(creature));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryAllowsChoosingAnEnchantment() {
        Card enchantment = new LuminousBonds();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(land, enchantment));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleCardsBottomsOnlyTheTopFourWithoutAChoice() {
        List<Card> topCards = List.of(new Plains(), new RighteousBlow(),
                new Plains(), new RighteousBlow());
        Card deeper = new HitchclawRecluse();
        harness.setLibrary(player1, List.of(topCards.get(0), topCards.get(1),
                topCards.get(2), topCards.get(3), deeper));
        castAndResolveEtb();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(5);
        assertThat(library.getFirst()).isSameAs(deeper);
        assertThat(library.subList(1, 5)).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new SumalaWoodshaper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
