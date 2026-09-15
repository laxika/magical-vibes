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

@CardUsed({
        RamosianCaptain.class,
        RamosianSergeant.class,
        RamosianLieutenant.class,
        RamosianCommander.class,
        RamosianSkyMarshal.class,
        DeadlyInsect.class,
        Brainstorm.class
})
class RamosianCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability offers Rebel permanents with mana value 4 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(
                new RamosianSergeant(),
                new RamosianLieutenant(),
                new RamosianCommander(),
                new RamosianSkyMarshal(),
                new DeadlyInsect(),
                new Brainstorm()));

        activateCaptain();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Ramosian Sergeant", "Ramosian Lieutenant", "Ramosian Commander");
    }

    @Test
    @DisplayName("The activated ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(new RamosianSergeant()));

        activateCaptain();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Captain", "Ramosian Sergeant");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(new DeadlyInsect(), new Brainstorm()));

        activateCaptain();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Captain");
    }

    @Test
    @DisplayName("Activating the ability pays five generic mana and taps Ramosian Captain")
    void activationPaysManaAndTapsCaptain() {
        Permanent captain = addReadyCaptain();
        harness.setLibrary(player1, List.of());

        activateCaptain();

        assertThat(captain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addReadyCaptain() {
        Permanent captain = addCreatureReady(player1, new RamosianCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        return captain;
    }

    private void activateCaptain() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
