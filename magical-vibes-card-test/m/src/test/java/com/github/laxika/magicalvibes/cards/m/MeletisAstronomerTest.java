package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeletisAstronomer.class, Forest.class, GiantGrowth.class, Shock.class, Solemnity.class})
class MeletisAstronomerTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic reveals an enchantment into hand and puts the rest on the library bottom")
    void heroicFindsEnchantmentAmongTopThree() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        Card instant = new Shock();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(enchantment, instant, land));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID astronomerId = harness.getPermanentId(player1, "Meletis Astronomer");
        harness.castAndResolveInstant(player1, 0, astronomerId);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(enchantment);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant, land);
    }

    @Test
    @DisplayName("Declining the heroic choice leaves the matching card on the library bottom")
    void mayDeclineEnchantmentReveal() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID astronomerId = harness.getPermanentId(player1, "Meletis Astronomer");
        harness.castAndResolveInstant(player1, 0, astronomerId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
    }

    @Test
    @DisplayName("An opponent's spell targeting the creature does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(enchantment));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID astronomerId = harness.getPermanentId(player1, "Meletis Astronomer");
        harness.castAndResolveInstant(player2, 0, astronomerId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
    }

    @Test
    @DisplayName("Heroic chooses only one enchantment from the top three and preserves the unseen library")
    void choosesOneEnchantmentAndOrdersRestBelowUnseenCards() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card firstEnchantment = new Solemnity();
        Card secondEnchantment = new Solemnity();
        Card land = new Forest();
        Card unseenEnchantment = new Solemnity();
        harness.setLibrary(player1, List.of(firstEnchantment, secondEnchantment, land, unseenEnchantment));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Meletis Astronomer"));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(firstEnchantment, secondEnchantment);
        harness.handleCardChosen(player1, 1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondEnchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseenEnchantment, land, firstEnchantment);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no matching enchantment, all looked-at cards go to the bottom in the chosen order")
    void noMatchingEnchantmentStillAllowsBottomOrdering() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card first = new Shock();
        Card second = new Forest();
        Card third = new GiantGrowth();
        Card unseen = new Solemnity();
        harness.setLibrary(player1, List.of(first, second, third, unseen));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Meletis Astronomer"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, third, first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Heroic resolves harmlessly with an empty library")
    void emptyLibraryDoesNotRequireAChoice() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Meletis Astronomer"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Heroic can take an enchantment when fewer than three cards remain")
    void takesEnchantmentFromShortLibrary() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card land = new Forest();
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(land, enchantment));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Meletis Astronomer"));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining an enchantment leaves all three cards available for bottom ordering")
    void decliningStillOrdersAllLookedAtCards() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        Card instant = new Shock();
        Card land = new Forest();
        Card unseen = new GiantGrowth();
        harness.setLibrary(player1, List.of(enchantment, instant, land, unseen));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Meletis Astronomer"));
        harness.handleCardChosen(player1, -1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, land, instant, enchantment);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger the astronomer's heroic ability")
    void targetingAnotherCreatureDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new MeletisAstronomer());
        harness.addToBattlefield(player2, new MeletisAstronomer());
        Card enchantment = new Solemnity();
        harness.setLibrary(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Meletis Astronomer"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
    }
}
