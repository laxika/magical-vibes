package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkSupplicant.class, GrizzlyBears.class})
class DarkSupplicantTest extends BaseCardTest {

    @Test
    @DisplayName("Requires three Clerics to activate")
    void requiresThreeClerics() {
        Permanent supplicant = harness.addToBattlefieldAndReturn(player1, new DarkSupplicant());
        supplicant.setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(supplicant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrifices three Clerics and returns Scion of Darkness from the graveyard")
    void returnsScionFromGraveyard() {
        addThreeSupplicants();
        Card scion = scionOfDarkness();
        harness.setGraveyard(player1, List.of(scion));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Scion of Darkness");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Dark Supplicant", "Dark Supplicant", "Dark Supplicant");
    }

    @Test
    @DisplayName("Searches the hand or library for Scion of Darkness")
    void searchesHandOrLibrary() {
        addThreeSupplicants();
        Card handScion = scionOfDarkness();
        harness.setHand(player1, List.of(handScion));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scion of Darkness");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        addThreeSupplicants();
        Card libraryScion = scionOfDarkness();
        harness.setLibrary(player1, List.of(libraryScion, new GrizzlyBears()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Scion of Darkness");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Scion of Darkness"));
    }

    private void addThreeSupplicants() {
        addCreatureReady(player1, new DarkSupplicant());
        addCreatureReady(player1, new DarkSupplicant());
        addCreatureReady(player1, new DarkSupplicant());
    }

    private Card scionOfDarkness() {
        Card scion = new Card();
        scion.setName("Scion of Darkness");
        scion.setType(CardType.CREATURE);
        scion.setManaCost("{7}{B}{B}");
        scion.setPower(6);
        scion.setToughness(6);
        return scion;
    }
}
