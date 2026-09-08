package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DanceOfTheTumbleweeds.class, DesertOfTheTrue.class, Forest.class, GrizzlyBears.class})
class DanceOfTheTumbleweedsTest extends BaseCardTest {

    @Test
    @DisplayName("The ramp mode searches for a basic land or Desert and puts it onto the battlefield")
    void rampModeSearchesBasicLandOrDesert() {
        Card forest = new Forest();
        Card desert = new DesertOfTheTrue();
        harness.setLibrary(player1, List.of(forest, desert, new GrizzlyBears()));

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
        harness.setLibrary(player1, List.of(fetchedForest, new GrizzlyBears()));

        cast(new int[]{0, 1}, 6);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(fetchedForest.getId()));
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
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
