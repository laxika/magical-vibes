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
