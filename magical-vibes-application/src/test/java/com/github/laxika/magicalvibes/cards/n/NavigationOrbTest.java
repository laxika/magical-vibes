package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NavigationOrb.class, Forest.class, RakdosGuildgate.class, GrizzlyBears.class})
class NavigationOrbTest extends BaseCardTest {

    @Test
    @DisplayName("It offers basic lands and Gates, then puts one tapped and one into hand")
    void searchesForBasicLandOrGate() {
        activateOrb(List.of(new Forest(), new RakdosGuildgate(), new GrizzlyBears()));

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch).isNotNull();
        assertThat(firstSearch.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(firstSearch.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Rakdos Guildgate");

        int gateIndex = firstSearch.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Rakdos Guildgate");
        harness.handleCardChosen(player1, gateIndex);

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch).isNotNull();
        assertThat(secondSearch.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(secondSearch.params().cards()).extracting(Card::getName)
                .containsExactly("Forest");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof RakdosGuildgate
                        && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
        harness.assertInGraveyard(player1, "Navigation Orb");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("It can fail to find when the library has no basic land or Gate")
    void canFailToFind() {
        activateOrb(List.of(new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Navigation Orb");
        harness.assertInGraveyard(player1, "Navigation Orb");
    }

    private void activateOrb(List<Card> library) {
        harness.addToBattlefield(player1, new NavigationOrb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
