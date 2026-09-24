package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cloudskate;
import com.github.laxika.magicalvibes.cards.r.RamosianCommander;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({DefiantFalcon.class, Cloudskate.class, Daze.class, RamosianCommander.class})
class DefiantFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 3 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyFalcon();
        DefiantFalcon matchingFalcon = new DefiantFalcon();
        harness.setLibrary(player1, List.of(
                matchingFalcon,
                new Cloudskate(),
                new Daze(),
                new RamosianCommander()));

        activateFalcon();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingFalcon);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        Permanent falcon = addReadyFalcon();
        DefiantFalcon foundFalcon = new DefiantFalcon();
        harness.setLibrary(player1, List.of(foundFalcon));

        activateFalcon();
        assertThat(falcon.isTapped()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard() == foundFalcon);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        Permanent falcon = addReadyFalcon();
        harness.setLibrary(player1, List.of(new Cloudskate(), new Daze()));

        activateFalcon();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .anyMatch(permanent -> permanent == falcon);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The controller may fail to find a matching Rebel")
    void mayFailToFindMatchingRebel() {
        Permanent falcon = addReadyFalcon();
        DefiantFalcon foundFalcon = new DefiantFalcon();
        harness.setLibrary(player1, List.of(foundFalcon, new Cloudskate(), new Daze()));

        activateFalcon();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .anyMatch(permanent -> permanent == falcon);
        assertThat(gd.playerDecks.get(player1.getId())).contains(foundFalcon);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The ability requires four mana")
    void requiresFourManaToActivate() {
        Permanent falcon = addCreatureReady(player1, new DefiantFalcon());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(falcon.isTapped()).isFalse();
    }

    private Permanent addReadyFalcon() {
        Permanent falcon = addCreatureReady(player1, new DefiantFalcon());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        return falcon;
    }

    private void activateFalcon() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
