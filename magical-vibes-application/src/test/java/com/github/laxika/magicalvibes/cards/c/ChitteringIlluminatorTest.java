package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitteringIlluminator.class, GrizzlyBears.class, Shock.class})
class ChitteringIlluminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a creature spell from the top of the library")
    void castsCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ChitteringIlluminator());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot cast a noncreature spell from the top of the library")
    void cannotCastNoncreatureFromTop() {
        harness.addToBattlefield(player1, new ChitteringIlluminator());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    @DisplayName("Can cast itself from the library top without a copy on the battlefield")
    void castsItselfFromLibraryTop() {
        Card illuminator = new ChitteringIlluminator();
        harness.setLibrary(player1, List.of(illuminator));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Chittering Illuminator");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(illuminator);
    }

    @Test
    @DisplayName("Its owner can privately see it on top without a copy on the battlefield")
    void seesItselfFromLibraryTop() {
        harness.setLibrary(player1, List.of(new ChitteringIlluminator()));

        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Chittering Illuminator"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    @DisplayName("A creature on top is visible only to the controller")
    void privatelySeesCreatureOnTop() {
        harness.addToBattlefield(player1, new ChitteringIlluminator());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    @DisplayName("A noncreature on top remains hidden even with Illuminator on the battlefield")
    void cannotSeeNoncreatureOnTop() {
        harness.addToBattlefield(player1, new ChitteringIlluminator());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    @DisplayName("An opponent's Illuminator does not grant permission to cast from your library")
    void opponentDoesNotGrantCastingPermission() {
        harness.addToBattlefield(player2, new ChitteringIlluminator());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }
}
