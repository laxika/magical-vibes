package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShelldockIsleTest extends BaseCardTest {

    /** Puts Shelldock Isle on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addIsleWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new ShelldockIsle());
        GameData gd = harness.getGameData();
        Permanent isle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shelldock Isle"))
                .findFirst().orElseThrow();
        gd.setImprintedCard(isle.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return isle;
    }

    /** A library with {@code count} filler cards. */
    private List<Card> libraryOf(int count) {
        return Collections.nCopies(count, new GrizzlyBears());
    }

    @Test
    @DisplayName("Plays the exiled card when a library has twenty or fewer cards")
    void playsExiledCardWithSmallLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(20));
        harness.setLibrary(player2, libraryOf(40));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability -> offers "may play"
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free-cast creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getImprintedCard(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shelldock Isle"))
                .findFirst().orElseThrow().getCard())).isNull();
    }

    @Test
    @DisplayName("An opponent's small library also satisfies the condition")
    void opponentSmallLibrarySatisfies() {
        GrizzlyBears bears = new GrizzlyBears();
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(40));
        harness.setLibrary(player2, libraryOf(15));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing while every library has more than twenty cards")
    void doesNothingWithLargeLibraries() {
        GrizzlyBears bears = new GrizzlyBears();
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(40));
        harness.setLibrary(player2, libraryOf(40));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability — condition not met

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        GrizzlyBears bears = new GrizzlyBears();
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(20));
        harness.setLibrary(player2, libraryOf(40));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
