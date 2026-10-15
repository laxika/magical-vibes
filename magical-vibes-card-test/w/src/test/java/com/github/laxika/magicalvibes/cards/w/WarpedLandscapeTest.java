package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarpedLandscape.class, Plains.class, Forest.class, DevilthornFox.class})
class WarpedLandscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds colorless mana")
    void manaAbilityAddsColorlessMana() {
        harness.addToBattlefield(player1, new WarpedLandscape());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Search ability costs two mana and sacrifices Warped Landscape")
    void searchAbilitySacrificesItself() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Warped Landscape");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Search ability offers only basic lands and puts the chosen land onto the battlefield tapped")
    void searchesForBasicLandToBattlefieldTapped() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new DevilthornFox()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND))
                .allMatch(card -> card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    void manaAbilityTapsImmediatelyWithoutUsingTheStack() {
        harness.addToBattlefield(player1, new WarpedLandscape());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Warped Landscape").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void coloredManaPaysGenericCostAndOnlyChosenLandMoves() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        harness.addMana(player1, ManaColor.WHITE, 2);
        Plains plains = new Plains();
        Forest forest = new Forest();
        WarpedLandscape nonbasicLand = new WarpedLandscape();
        harness.setLibrary(player1, List.of(plains, forest, nonbasicLand));
        Plains opponentsLand = new Plains();
        harness.setLibrary(player2, List.of(opponentsLand));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertNotOnBattlefield(player1, "Warped Landscape");
        harness.assertInGraveyard(player1, "Warped Landscape");
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains, forest);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, nonbasicLand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void cannotSearchWithOnlyOneMana() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Warped Landscape");
        assertThat(findPermanent(player1, "Warped Landscape").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSearchWhenTapped() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        findPermanent(player1, "Warped Landscape").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Warped Landscape");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayFailToFindEvenWithABasicLandAvailable() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Warped Landscape");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void nonbasicLandCannotBeFound() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        WarpedLandscape nonbasicLand = new WarpedLandscape();
        harness.setLibrary(player1, List.of(nonbasicLand));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Warped Landscape");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void searchResolvesWithAnEmptyLibrary() {
        harness.addToBattlefield(player1, new WarpedLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Warped Landscape");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }
}
