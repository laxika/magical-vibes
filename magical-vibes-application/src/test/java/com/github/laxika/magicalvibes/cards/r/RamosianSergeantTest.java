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
