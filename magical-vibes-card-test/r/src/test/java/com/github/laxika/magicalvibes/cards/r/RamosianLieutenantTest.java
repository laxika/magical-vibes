package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RamosianLieutenant.class, RamosianCaptain.class, RamosianCommander.class,
        DeadlyInsect.class, Brainstorm.class})
class RamosianLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 3 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyLieutenant();
        harness.setLibrary(player1, List.of(
                new RamosianCaptain(),
                new RamosianCommander(),
                new DeadlyInsect(),
                new Brainstorm()));

        activateLieutenant();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Ramosian Captain");
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyLieutenant();
        harness.setLibrary(player1, List.of(new RamosianCaptain()));

        activateLieutenant();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Lieutenant", "Ramosian Captain");
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        addReadyLieutenant();
        harness.setLibrary(player1, List.of(new DeadlyInsect(), new Brainstorm()));

        activateLieutenant();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Lieutenant");
    }

    @Test
    @DisplayName("Activating the ability pays four generic mana and taps Ramosian Lieutenant")
    void activationPaysManaAndTapsLieutenant() {
        Permanent lieutenant = addReadyLieutenant();
        harness.setLibrary(player1, List.of());

        activateLieutenant();

        assertThat(lieutenant.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A matching Rebel may be left in the library and the library is still shuffled")
    void mayFailToFindMatchingRebel() {
        addReadyLieutenant();
        RamosianCaptain captain = new RamosianCaptain();
        harness.setLibrary(player1, List.of(captain));

        activateLieutenant();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(captain);
        assertThat(countPermanents(player1, "Ramosian Captain")).isZero();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The search uses only the controller's library and puts one Rebel onto the battlefield untapped")
    void searchesOnlyControllersLibraryAndPutsOneRebelOntoBattlefield() {
        addReadyLieutenant();
        RamosianCaptain captain = new RamosianCaptain();
        RamosianLieutenant remainingRebel = new RamosianLieutenant();
        RamosianCaptain opponentsCaptain = new RamosianCaptain();
        harness.setLibrary(player1, List.of(captain, remainingRebel));
        harness.setLibrary(player2, List.of(opponentsCaptain));

        activateLieutenant();
        harness.handleCardChosen(player1, 0);

        Permanent recruited = findPermanent(player1, "Ramosian Captain");
        assertThat(recruited.getCard()).isSameAs(captain);
        assertThat(recruited.isTapped()).isFalse();
        assertThat(recruited.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingRebel);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCaptain);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private Permanent addReadyLieutenant() {
        Permanent lieutenant = addCreatureReady(player1, new RamosianLieutenant());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        return lieutenant;
    }

    private void activateLieutenant() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
