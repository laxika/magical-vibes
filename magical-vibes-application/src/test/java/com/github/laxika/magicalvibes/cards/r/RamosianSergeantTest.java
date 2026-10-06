package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CharmPeddler;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({
        RamosianSergeant.class,
        FreshVolunteers.class,
        RamosianCaptain.class,
        CharmPeddler.class,
        RamosianRally.class
})
class RamosianSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 2 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadySergeant();
        harness.setLibrary(player1, List.of(
                new RamosianSergeant(),
                new FreshVolunteers(),
                new RamosianCaptain(),
                new CharmPeddler(),
                new RamosianRally()));

        activateSergeant();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Ramosian Sergeant", "Fresh Volunteers");
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadySergeant();
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        activateSergeant();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Sergeant", "Fresh Volunteers");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        addReadySergeant();
        harness.setLibrary(player1, List.of(
                new RamosianCaptain(),
                new CharmPeddler(),
                new RamosianRally()));

        activateSergeant();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Sergeant");
    }

    @Test
    @DisplayName("Activating the ability pays three generic mana and taps Ramosian Sergeant")
    void activationPaysManaAndTapsSergeant() {
        Permanent sergeant = addReadySergeant();
        harness.setLibrary(player1, List.of());

        activateSergeant();

        assertThat(sergeant.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The search may fail to find even when a matching Rebel is present")
    void mayDeclineMatchingRebel() {
        addReadySergeant();
        FreshVolunteers volunteers = new FreshVolunteers();
        harness.setLibrary(player1, List.of(volunteers));

        activateSergeant();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(volunteers);
        harness.assertNotOnBattlefield(player1, "Fresh Volunteers");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still resolves after Ramosian Sergeant leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent sergeant = addReadySergeant();
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(sergeant);
        gd.playerGraveyards.get(player1.getId()).add(sergeant.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        harness.assertNotOnBattlefield(player1, "Ramosian Sergeant");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Fresh Volunteers").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Fresh Volunteers").isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Ramosian Sergeant cannot activate its tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent sergeant = addReadySergeant();
        sergeant.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(sergeant.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Ramosian Sergeant cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent sergeant = addReadySergeant();
        sergeant.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only one Rebel is found and the opponent's library is untouched")
    void findsOneRebelFromControllersLibrary() {
        addReadySergeant();
        FreshVolunteers first = new FreshVolunteers();
        FreshVolunteers second = new FreshVolunteers();
        RamosianSergeant opposingRebel = new RamosianSergeant();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opposingRebel));

        activateSergeant();
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Fresh Volunteers")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingRebel);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySergeant() {
        Permanent sergeant = addCreatureReady(player1, new RamosianSergeant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        return sergeant;
    }

    private void activateSergeant() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
