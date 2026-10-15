package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.h.HashepOasis;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
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

@CardUsed({BeneathTheSands.class, Plains.class, Forest.class, Island.class, FeralProwler.class, HashepOasis.class})
class BeneathTheSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic lands, destination battlefield tapped")
    void resolvesOffersBasicLandsToBattlefieldTapped() {
        harness.setHand(player1, List.of(new BeneathTheSands()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        setupLibrary();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Chosen basic land enters battlefield tapped")
    void chosenBasicLandEntersTapped() {
        harness.setHand(player1, List.of(new BeneathTheSands()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        setupLibrary();
        harness.castAndResolveSorcery(player1, 0, 0);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        harness.assertInGraveyard(player1, "Beneath the Sands");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cycling {2} discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BeneathTheSands()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Beneath the Sands");
        harness.assertInHand(player1, "Feral Prowler");
    }

    @Test
    @DisplayName("A basic land search may fail to find even when basic lands exist")
    void mayFailToFind() {
        harness.setHand(player1, List.of(new BeneathTheSands()));
        setupLibrary();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        harness.assertInGraveyard(player1, "Beneath the Sands");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No basic land in the library still allows the spell to finish")
    void resolvesWithoutBasicLands() {
        harness.setHand(player1, List.of(new BeneathTheSands()));
        harness.setLibrary(player1, List.of(new HashepOasis(), new FeralProwler()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Beneath the Sands");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library finishes normally")
    void resolvesWithEmptyLibrary() {
        harness.setHand(player1, List.of(new BeneathTheSands()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Beneath the Sands");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards as a cost and waits for resolution to draw")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new BeneathTheSands()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Beneath the Sands");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Feral Prowler");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new FeralProwler(), new HashepOasis()));
    }
}
