package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntrepidPaleontologist;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtalisFavor.class, Forest.class, IntrepidPaleontologist.class, PanickedAltisaur.class, QuintoriusKand.class})
class EtalisFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and trample")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent creature = castOnOwnCreature(List.of());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("When Etali's Favor enters, discover 3 and put the found card into hand")
    void discoversThreeAndPutsFoundCardIntoHand() {
        IntrepidPaleontologist discovered = new IntrepidPaleontologist();
        Permanent creature = castOnOwnCreature(List.of(new Forest(), discovered));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Discover 3 can cast the found card without paying its mana cost")
    void castsDiscoveredCardForFree() {
        IntrepidPaleontologist discovered = new IntrepidPaleontologist();
        castOnOwnCreature(List.of(discovered));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Etali's Favor can enchant only a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IntrepidPaleontologist());
        harness.setHand(player1, List.of(new EtalisFavor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Discover skips lands and cards above mana value three and bottoms only the skipped cards")
    void skipsIneligibleCardsAndPreservesUnsearchedLibrary() {
        Forest land = new Forest();
        PanickedAltisaur expensive = new PanickedAltisaur();
        EtalisFavor discovered = new EtalisFavor();
        Forest untouched = new Forest();
        castOnOwnCreature(List.of(land, expensive, discovered, untouched));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Discover exiles both skipped cards and the qualifying card before the choice")
    void cardsAreInExileWhileDiscoverChoiceIsPending() {
        Forest land = new Forest();
        IntrepidPaleontologist discovered = new IntrepidPaleontologist();
        castOnOwnCreature(List.of(land, discovered));

        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactlyInAnyOrder(land, discovered);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Discover returns every card when there is no qualifying nonland card")
    void noQualifyingCardReturnsLibraryWithoutOfferingChoice() {
        Forest land = new Forest();
        PanickedAltisaur expensive = new PanickedAltisaur();
        castOnOwnCreature(List.of(land, expensive));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discover with an empty library completes without offering a choice")
    void emptyLibraryCompletesDiscover() {
        castOnOwnCreature(List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A discovered Etali's Favor targets a creature and triggers discover again")
    void discoveredAuraResolvesAndDiscoversAgain() {
        EtalisFavor discovered = new EtalisFavor();
        Permanent creature = castOnOwnCreature(List.of(discovered));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Casting a discovered creature triggers Quintorius Kand's cast-from-exile ability")
    void discoveredCreatureIsCastFromExile() {
        harness.addToBattlefield(player1, new QuintoriusKand());
        IntrepidPaleontologist discovered = new IntrepidPaleontologist();
        castOnOwnCreature(List.of(discovered));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting a discovered Aura triggers Quintorius Kand's cast-from-exile ability")
    void discoveredAuraIsCastFromExile() {
        harness.addToBattlefield(player1, new QuintoriusKand());
        EtalisFavor discovered = new EtalisFavor();
        Permanent creature = castOnOwnCreature(List.of(discovered));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private Permanent castOnOwnCreature(List<com.github.laxika.magicalvibes.model.Card> library) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidPaleontologist());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EtalisFavor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }
}
