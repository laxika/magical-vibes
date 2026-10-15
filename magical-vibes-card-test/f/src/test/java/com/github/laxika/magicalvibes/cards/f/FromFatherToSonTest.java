package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AdventurersAirship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FromFatherToSon.class, AdventurersAirship.class})
class FromFatherToSonTest extends BaseCardTest {

    @Test
    @DisplayName("A normal cast puts a Vehicle into hand")
    void normalCastPutsVehicleIntoHand() {
        Card vehicle = new AdventurersAirship();
        harness.setHand(player1, List.of(new FromFatherToSon()));
        harness.setLibrary(player1, List.of(new FromFatherToSon(), vehicle));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(vehicle);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(vehicle);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().equals(vehicle));
    }

    @Test
    @DisplayName("Flashback puts a Vehicle onto the battlefield")
    void flashbackPutsVehicleOntoBattlefield() {
        Card vehicle = new AdventurersAirship();
        harness.setGraveyard(player1, List.of(new FromFatherToSon()));
        harness.setLibrary(player1, List.of(new FromFatherToSon(), vehicle));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, null);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(vehicle);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(vehicle));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(vehicle);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Only the selected Vehicle leaves the library")
    void selectsExactlyOneVehicle(boolean flashback) {
        Card spell = new FromFatherToSon();
        Card unchosen = new AdventurersAirship();
        Card chosen = new AdventurersAirship();
        harness.setLibrary(player1, List.of(unchosen, chosen));

        castAndResolve(spell, flashback);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        if (flashback) {
            assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                    .satisfies(permanent -> {
                        assertThat(permanent.getCard()).isSameAs(chosen);
                        assertThat(permanent.isTapped()).isFalse();
                    });
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosen, unchosen);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }
        assertFinishedInExpectedZone(spell, flashback);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("A restricted search may fail to find even when a Vehicle is present")
    void mayFailToFindVehicle(boolean flashback) {
        Card spell = new FromFatherToSon();
        Card vehicle = new AdventurersAirship();
        harness.setLibrary(player1, List.of(vehicle));

        castAndResolve(spell, flashback);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vehicle);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(vehicle);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertFinishedInExpectedZone(spell, flashback);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("A library with no Vehicles resolves without finding a card")
    void resolvesWithoutMatchingCards(boolean flashback) {
        Card spell = new FromFatherToSon();
        Card nonVehicle = new FromFatherToSon();
        harness.setLibrary(player1, List.of(nonVehicle));

        castAndResolve(spell, flashback);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonVehicle);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonVehicle);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertFinishedInExpectedZone(spell, flashback);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("An empty library does not prevent the spell from finishing")
    void resolvesWithEmptyLibrary(boolean flashback) {
        Card spell = new FromFatherToSon();
        harness.setLibrary(player1, List.of());

        castAndResolve(spell, flashback);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertFinishedInExpectedZone(spell, flashback);
    }

    private void castAndResolve(Card spell, boolean flashback) {
        harness.setHand(player1, flashback ? List.of() : List.of(spell));
        harness.setGraveyard(player1, flashback ? List.of(spell) : List.of());
        harness.addMana(player1, ManaColor.WHITE, flashback ? 3 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, flashback ? 4 : 1);
        if (flashback) {
            harness.castAndResolveFlashback(player1, 0, null);
        } else {
            harness.castAndResolveSorcery(player1, 0, 0);
        }
    }

    private void assertFinishedInExpectedZone(Card spell, boolean flashback) {
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        if (flashback) {
            assertThat(gd.findExiledCard(spell.getId())).isNotNull();
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        } else {
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
            assertThat(gd.findExiledCard(spell.getId())).isNull();
        }
    }
}
