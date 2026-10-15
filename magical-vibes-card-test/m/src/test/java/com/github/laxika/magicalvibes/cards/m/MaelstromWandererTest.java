package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaelstromWanderer.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Unsummon.class})
class MaelstromWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to creatures its controller controls, including itself")
    void grantsHasteToOwnCreatures() {
        harness.addToBattlefield(player1, new MaelstromWanderer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Maelstrom Wanderer"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cascades twice when cast")
    void cascadesTwice() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(new HillGiant(), new LlanowarElves()));

        harness.setHand(player1, List.of(new MaelstromWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertCascadeOffers("Hill Giant");

        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();
        assertCascadeOffers("Llanowar Elves");
    }

    @Test
    @DisplayName("Each cascaded creature resolves before the next cascade and Wanderer")
    void castsBothCascadeHitsBeforeWandererResolves() {
        harness.setLibrary(player1, List.of(new HillGiant(), new LlanowarElves()));
        castWanderer();

        harness.passBothPriorities();
        assertCascadeOffers("Hill Giant");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Maelstrom Wanderer");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Hill Giant"), Keyword.HASTE)).isFalse();

        harness.passBothPriorities();
        assertCascadeOffers("Llanowar Elves");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Maelstrom Wanderer");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Maelstrom Wanderer");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Hill Giant"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Llanowar Elves"), Keyword.HASTE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cascade skips equal mana values and returns declined cards to the bottom")
    void skipsEqualManaValueAndBottomsDeclinedCard() {
        MaelstromWanderer equalManaValue = new MaelstromWanderer();
        HillGiant firstHit = new HillGiant();
        LlanowarElves secondHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(equalManaValue, firstHit, secondHit));
        castWanderer();

        harness.passBothPriorities();
        assertCascadeOffers("Hill Giant");
        assertThat(gd.findExiledCard(equalManaValue.getId())).isNotNull();
        assertThat(gd.findExiledCard(firstHit.getId())).isNotNull();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondHit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equalManaValue, firstHit, secondHit);
        assertThat(gd.findExiledCard(equalManaValue.getId())).isNull();
        assertThat(gd.findExiledCard(firstHit.getId())).isNull();

        harness.passBothPriorities();
        assertCascadeOffers("Llanowar Elves");
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Maelstrom Wanderer");
    }

    @Test
    @DisplayName("Both cascades do nothing with an empty library and Wanderer still resolves")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castWanderer();

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Maelstrom Wanderer");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Granted haste ends when Wanderer leaves the battlefield")
    void hasteEndsWhenWandererLeaves() {
        harness.addToBattlefield(player1, new MaelstromWanderer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Maelstrom Wanderer"));

        harness.assertNotOnBattlefield(player1, "Maelstrom Wanderer");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    private void castWanderer() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MaelstromWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

    private void assertCascadeOffers(String cardName) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly(cardName);
    }
}
