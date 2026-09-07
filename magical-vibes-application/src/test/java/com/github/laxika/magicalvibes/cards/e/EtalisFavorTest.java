package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({EtalisFavor.class, Forest.class, GrizzlyBears.class})
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
        GrizzlyBears discovered = new GrizzlyBears();
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
        GrizzlyBears discovered = new GrizzlyBears();
        castOnOwnCreature(List.of(discovered));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Etali's Favor can enchant only a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EtalisFavor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent castOnOwnCreature(List<com.github.laxika.magicalvibes.model.Card> library) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EtalisFavor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }
}
