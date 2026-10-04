package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SacredFoundry;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Farseek.class, Plains.class, Island.class, Swamp.class, Mountain.class, Forest.class,
        SacredFoundry.class, BirdsOfParadise.class, TempleGarden.class})
class FarseekTest extends BaseCardTest {

    @Test
    @DisplayName("Offers cards with Plains, Island, Swamp or Mountain types, but not Forest or nonlands")
    void offersNonForestLandTypes() {
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new Swamp(), new Mountain(),
                new SacredFoundry(), new Forest(), new BirdsOfParadise()));

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Island", "Swamp", "Mountain", "Sacred Foundry");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Chosen land enters the battlefield tapped")
    void chosenLandEntersTapped() {
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new Swamp(), new Mountain(),
                new SacredFoundry(), new Forest(), new BirdsOfParadise()));

        harness.castAndResolveSorcery(player1, 0, 0);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find and still finish when the library has no matching land type")
    void noMatchingLandTypeFinishesSearch() {
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest(), new BirdsOfParadise()));

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can decline to find even when an eligible land is present")
    void canDeclineEligibleLand() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(plains, forest));
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void emptyLibraryResolves() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A Forest Plains is eligible and remains tapped even when its life payment is made")
    void forestDualLandRemainsTappedAfterLifePayment() {
        TempleGarden garden = new TempleGarden();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, garden));
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Temple Garden").getCard()).isSameAs(garden);
        assertThat(findPermanent(player1, "Temple Garden").isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searches only the caster's library and puts exactly one land under their control")
    void chosenLandLeavesOnlyCastersLibrary() {
        Island island = new Island();
        Swamp swamp = new Swamp();
        Mountain opponentsMountain = new Mountain();
        harness.setLibrary(player1, List.of(island, swamp));
        harness.setLibrary(player2, List.of(opponentsMountain));
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        int opponentBattlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Swamp").getCard()).isSameAs(swamp);
        assertThat(findPermanent(player1, "Swamp").isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsMountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(opponentBattlefieldBefore);
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
