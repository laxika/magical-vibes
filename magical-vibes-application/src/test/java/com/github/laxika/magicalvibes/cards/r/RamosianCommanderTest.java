package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.cards.l.LastBreath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RamosianCommander.class, FreshVolunteers.class, RamosianCaptain.class, JhovallRider.class,
        JhovallQueen.class, DeadlyInsect.class, LastBreath.class})
class RamosianCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability pays six generic mana and taps Ramosian Commander")
    void activationPaysManaAndTapsCommander() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        activateCommander();

        assertThat(findPermanent(player1, "Ramosian Commander").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The activated ability offers Rebel permanents with mana value 5 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(
                new FreshVolunteers(),
                new RamosianCaptain(),
                new JhovallRider(),
                new JhovallQueen(),
                new DeadlyInsect(),
                new LastBreath()));

        activateCommander();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Fresh Volunteers", "Ramosian Captain", "Jhovall Rider");
    }

    @Test
    @DisplayName("The activated ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        activateCommander();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Commander", "Fresh Volunteers");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(new JhovallQueen(), new DeadlyInsect(), new LastBreath()));

        activateCommander();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Commander");
    }

    private void addReadyCommander() {
        addCreatureReady(player1, new RamosianCommander());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void activateCommander() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
