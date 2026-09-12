package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraidwoodSextant.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class BraidwoodSextantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and searches a basic land into its controller's hand")
    void sacrificesAndSearchesBasicLandToHand() {
        harness.addToBattlefield(player1, new BraidwoodSextant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Braidwood Sextant");
        harness.assertInGraveyard(player1, "Braidwood Sextant");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(chosen.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).doesNotContain(chosen.getId());
    }

    @Test
    @DisplayName("Cannot activate while Braidwood Sextant is tapped")
    void cannotActivateWhileTapped() {
        Permanent sextant = harness.addToBattlefieldAndReturn(player1, new BraidwoodSextant());
        sextant.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(sextant);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic activation cost")
    void cannotActivateWithoutEnoughMana() {
        Permanent sextant = harness.addToBattlefieldAndReturn(player1, new BraidwoodSextant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(sextant);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("A library with no basic lands completes without a choice")
    void noBasicLandsCompletesWithoutChoice() {
        harness.addToBattlefield(player1, new BraidwoodSextant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<Card> library = List.of(new GrizzlyBears());
        harness.setLibrary(player1, library);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Braidwood Sextant");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(library.getFirst().getId());
    }

    @Test
    @DisplayName("The controller may fail to find a basic land")
    void mayFailToFindBasicLand() {
        harness.addToBattlefield(player1, new BraidwoodSextant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<Card> library = List.of(new Plains(), new Forest(), new Island());
        harness.setLibrary(player1, library);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(library.stream().map(Card::getId).toList());
    }
}
