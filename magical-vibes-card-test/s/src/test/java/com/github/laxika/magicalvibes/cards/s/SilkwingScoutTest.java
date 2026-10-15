package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusChancery;
import com.github.laxika.magicalvibes.cards.p.Plains;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilkwingScout.class, Plains.class, AzoriusChancery.class})
class SilkwingScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Silkwing Scout sacrifices it and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        addScout();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Silkwing Scout");
        harness.assertInGraveyard(player1, "Silkwing Scout");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability searches for a basic land and puts it onto the battlefield tapped")
    void searchesForBasicLandAndPutsItTapped() {
        activateSearch(List.of(new Plains(), new AzoriusChancery()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability can fail to find a basic land")
    void canFailToFindBasicLand() {
        activateSearch(List.of(new AzoriusChancery()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The search may fail to find a basic land even when one is available")
    void mayFailToFindBasicLand() {
        Plains plains = new Plains();
        activateSearch(List.of(plains));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
    }

    @Test
    @DisplayName("The ability cannot be activated without green mana")
    void cannotActivateWithoutGreenMana() {
        addScout();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability resolves normally with an empty library")
    void resolvesWithEmptyLibrary() {
        activateSearch(List.of());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Silkwing Scout");
    }

    @Test
    @DisplayName("A tapped Scout can activate its ability the turn it enters")
    void tappedSummoningSickScoutCanActivate() {
        harness.castFromHand(player1, new SilkwingScout(), "{2}{U}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Silkwing Scout");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(plains);
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    @Test
    @DisplayName("The search takes exactly one land from its controller's library")
    void takesOnlyOneLandFromControllersLibrary() {
        Plains chosen = new Plains();
        Plains remaining = new Plains();
        Plains opponentsLand = new Plains();
        harness.setLibrary(player2, List.of(opponentsLand));
        activateSearch(List.of(chosen, remaining));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(chosen);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addScout() {
        harness.addToBattlefield(player1, new SilkwingScout());
    }

    private void activateSearch(List<Card> library) {
        addScout();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
    }
}
