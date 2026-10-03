package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Goldhound;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutYourLosses.class, CivicGardener.class, Goldhound.class})
class CutYourLossesTest extends BaseCardTest {

    @Test
    @DisplayName("Mills half the target player's library, rounded down")
    void millsHalfRoundedDown() {
        harness.setLibrary(player2, libraryOf(9));
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Casualty copies the spell and mills the remaining library separately")
    void casualtyCopiesSpell() {
        Permanent casualtyCreature = addCreatureReady(player1, new CivicGardener());
        harness.setLibrary(player2, libraryOf(10));
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Cannot pay casualty with a creature below the required power")
    void rejectsUnderpoweredCasualtyCreature() {
        Permanent casualtyCreature = addCreatureReady(player1, new Goldhound());
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, player2.getId(), casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2");
    }

    @Test
    @DisplayName("Casualty copy can target the caster without changing the original target")
    void casualtyCopyCanChooseNewTarget() {
        Permanent casualtyCreature = addCreatureReady(player1, new CivicGardener());
        harness.setLibrary(player1, libraryOf(9));
        harness.setLibrary(player2, libraryOf(10));
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(6)
                .filteredOn(card -> card instanceof CutYourLosses).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Small libraries are halved without rounding up or causing a draw loss")
    void millsSmallLibrary(int librarySize) {
        harness.setLibrary(player2, libraryOf(librarySize));
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - librarySize / 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(librarySize / 2);
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The caster can target themselves without paying casualty")
    void canMillOwnLibrary() {
        harness.setLibrary(player1, libraryOf(8));
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the casualty cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent casualtyCreature = addCreatureReady(player2, new CivicGardener());
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, player2.getId(), casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(casualtyCreature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private List<Card> libraryOf(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new CivicGardener())
                .toList();
    }
}
