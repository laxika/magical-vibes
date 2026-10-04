package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ScionOfDarkness;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkSupplicant.class, ScionOfDarkness.class, PsychogenicProbe.class})
class DarkSupplicantTest extends BaseCardTest {

    @Test
    @DisplayName("Requires three Clerics to activate")
    void requiresThreeClerics() {
        Permanent supplicant = addCreatureReady(player1, new DarkSupplicant());
        harness.addToBattlefield(player1, new ScionOfDarkness());
        harness.addToBattlefield(player1, new ScionOfDarkness());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(supplicant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrifices exactly three Clerics when more than three are available")
    void sacrificesExactlyThreeClericsWhenMoreAreAvailable() {
        Permanent source = addCreatureReady(player1, new DarkSupplicant());
        Permanent second = addCreatureReady(player1, new DarkSupplicant());
        Permanent third = addCreatureReady(player1, new DarkSupplicant());
        Permanent fourth = addCreatureReady(player1, new DarkSupplicant());
        ScionOfDarkness scion = scionOfDarkness();
        harness.setGraveyard(player1, List.of(scion));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.handlePermanentChosen(player1, fourth.getId());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(scion.getId()));

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Dark Supplicant", "Scion of Darkness");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Dark Supplicant", "Dark Supplicant", "Dark Supplicant");
    }

    @Test
    @DisplayName("Sacrifices three Clerics and returns Scion of Darkness from the graveyard")
    void returnsScionFromGraveyard() {
        addThreeSupplicants();
        Card scion = scionOfDarkness();
        harness.setGraveyard(player1, List.of(scion));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(scion.getId()));

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

        harness.handleMultipleCardsChosen(player1, List.of(handScion.getId()));
        harness.assertOnBattlefield(player1, "Scion of Darkness");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        addThreeSupplicants();
        Card libraryScion = scionOfDarkness();
        harness.setLibrary(player1, List.of(libraryScion, new DarkSupplicant()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(libraryScion.getId()));

        harness.assertOnBattlefield(player1, "Scion of Darkness");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Scion of Darkness"));
    }

    @Test
    @DisplayName("Only searches the activating player's zones")
    void onlySearchesActivatingPlayersZones() {
        addThreeSupplicants();
        ScionOfDarkness opponentScion = scionOfDarkness();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentScion));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(opponentScion.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentScion);
    }

    @Test
    @DisplayName("Allows choosing which searchable zone to use")
    void allowsChoosingWhichSearchableZoneToUse() {
        addThreeSupplicants();
        ScionOfDarkness graveyardScion = scionOfDarkness();
        ScionOfDarkness libraryScion = scionOfDarkness();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardScion));
        harness.setLibrary(player1, List.of(libraryScion));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .as("the controller must choose among matching cards in the graveyard and library")
                .isInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(libraryScion.getId()));
        harness.assertOnBattlefield(player1, "Scion of Darkness");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardScion);
    }

    @ParameterizedTest
    @EnumSource(value = Zone.class, names = {"HAND", "GRAVEYARD", "LIBRARY"})
    @DisplayName("Can retrieve Scion without searching the library, or search it and trigger shuffle damage")
    void shufflesOnlyWhenChoosingToSearchLibrary(Zone searchZone) {
        addThreeSupplicants();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        ScionOfDarkness scion = scionOfDarkness();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new DarkSupplicant()));
        switch (searchZone) {
            case HAND -> harness.setHand(player1, List.of(scion));
            case GRAVEYARD -> harness.setGraveyard(player1, List.of(scion));
            case LIBRARY -> harness.setLibrary(player1, List.of(scion, new DarkSupplicant()));
            default -> throw new IllegalArgumentException("Unexpected search zone");
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(scion.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Scion of Darkness");
        harness.assertLife(player1, searchZone == Zone.LIBRARY ? 18 : 20);
    }

    @Test
    @DisplayName("Cannot sacrifice opposing Clerics to pay the activation cost")
    void cannotSacrificeOpponentsClerics() {
        Permanent source = addCreatureReady(player1, new DarkSupplicant());
        addCreatureReady(player2, new DarkSupplicant());
        addCreatureReady(player2, new DarkSupplicant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");

        assertThat(source.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Dark Supplicant")).isEqualTo(1);
        assertThat(countPermanents(player2, "Dark Supplicant")).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped Dark Supplicant cannot activate even with three Clerics")
    void cannotActivateWhileTapped() {
        addThreeSupplicants();
        findPermanent(player1, "Dark Supplicant").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(countPermanents(player1, "Dark Supplicant")).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick Dark Supplicant cannot activate")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DarkSupplicant());
        addCreatureReady(player1, new DarkSupplicant());
        addCreatureReady(player1, new DarkSupplicant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(countPermanents(player1, "Dark Supplicant")).isEqualTo(3);
        assertThat(findPermanent(player1, "Dark Supplicant").isTapped()).isFalse();
    }

    private void addThreeSupplicants() {
        addCreatureReady(player1, new DarkSupplicant());
        addCreatureReady(player1, new DarkSupplicant());
        addCreatureReady(player1, new DarkSupplicant());
    }

    private ScionOfDarkness scionOfDarkness() {
        return new ScionOfDarkness();
    }
}
