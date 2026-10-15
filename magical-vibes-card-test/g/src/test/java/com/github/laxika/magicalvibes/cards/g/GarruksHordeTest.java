package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarruksHorde.class, GrizzlyBears.class, Shock.class})
class GarruksHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a creature spell from the top of the library")
    void castsCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot cast a creature from the top without Garruk's Horde on the battlefield")
    void cannotCastFromTopWithoutHorde() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Cannot cast a noncreature spell from the top of the library")
    void cannotCastNoncreatureFromTop() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    void revealsOnlyControllersTopCardToBothPlayers() {
        harness.addToBattlefield(player1, new GarruksHorde());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new GarruksHorde()));
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Shock\"")
                        && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Shock\"")
                        && message.contains("}],[]]"));
    }

    @Test
    void stopsRevealingAndGrantingPermissionWhenHordeLeaves() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card creature = new GarruksHorde();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 7);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void mustPayCreatureManaCost() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card creature = new GarruksHorde();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantFlash() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card creature = new GarruksHorde();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void opponentsHordeDoesNotGrantCastingPermission() {
        harness.addToBattlefield(player2, new GarruksHorde());
        Card creature = new GarruksHorde();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void canCastSuccessiveCreaturesAndRevealsNewTopImmediately() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card first = new GarruksHorde();
        Card second = new GarruksHorde();
        Card next = new Shock();
        harness.setLibrary(player1, List.of(first, second, next));
        harness.addMana(player1, ManaColor.GREEN, 14);
        harness.castAndResolveFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);
        harness.clearMessages();
        harness.publishState();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThat(gd.stack).hasSize(1);
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Shock\""));

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Garruk's Horde")).isEqualTo(3);
    }
}
