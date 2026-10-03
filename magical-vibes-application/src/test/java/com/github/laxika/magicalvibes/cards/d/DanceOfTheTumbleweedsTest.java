package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cactarantula;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SandstormVerge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanceOfTheTumbleweeds.class, SandstormVerge.class, Forest.class, Cactarantula.class})
class DanceOfTheTumbleweedsTest extends BaseCardTest {

    @Test
    @DisplayName("The ramp mode searches for a basic land or Desert and puts it onto the battlefield")
    void rampModeSearchesBasicLandOrDesert() {
        Card forest = new Forest();
        Card desert = new SandstormVerge();
        harness.setLibrary(player1, List.of(forest, desert, new Cactarantula()));

        cast(new int[]{0}, 3);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().cards()).containsExactly(forest, desert);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(desert.getId()));
    }

    @Test
    @DisplayName("The token mode creates an Elemental whose power and toughness equal your land count")
    void tokenModeUsesControlledLandCount() {
        addForests(player1, 3);
        addForests(player2, 5);

        cast(new int[]{1}, 5);

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Both Spree modes resolve and the fetched land counts toward the token size")
    void bothModesResolve() {
        addForests(player1, 2);
        Card fetchedForest = new Forest();
        harness.setLibrary(player1, List.of(fetchedForest, new Cactarantula()));

        cast(new int[]{0, 1}, 6);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(fetchedForest.getId()));
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("A fetched basic land enters untapped and the ramp mode does not create a token")
    void basicLandEntersUntapped() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        cast(new int[]{0}, 3);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The search may fail to find and the token mode still resolves")
    void failedSearchStillCreatesToken() {
        addForests(player1, 2);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        cast(new int[]{0, 1}, 6);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both modes finish when the library contains no matching cards")
    void noMatchingCardsStillCreatesToken() {
        addForests(player1, 1);
        Card creature = new Cactarantula();
        harness.setLibrary(player1, List.of(creature));

        cast(new int[]{0, 1}, 6);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Elemental"))).isEqualTo(1);
    }

    @Test
    @DisplayName("Both modes finish when the library is empty")
    void emptyLibraryStillCreatesToken() {
        addForests(player1, 1);
        harness.setLibrary(player1, List.of());

        cast(new int[]{0, 1}, 6);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Elemental"))).isEqualTo(1);
    }

    @Test
    @DisplayName("The token's size is fixed when created, rather than changing with the land count")
    void tokenSizeDoesNotChangeWithLaterLands() {
        addForests(player1, 2);
        harness.addToBattlefield(player1, new Cactarantula());
        Card libraryForest = new Forest();
        harness.setLibrary(player1, List.of(libraryForest));

        cast(new int[]{1}, 5);
        Permanent token = findPermanent(player1, "Elemental");
        harness.addToBattlefield(player1, new SandstormVerge());

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryForest);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("With no lands, the zero-toughness Elemental dies after resolution")
    void zeroLandTokenDies() {
        cast(new int[]{1}, 5);

        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof DanceOfTheTumbleweeds);
    }

    private void cast(int[] modes, int totalMana) {
        harness.setHand(player1, List.of(new DanceOfTheTumbleweeds()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
