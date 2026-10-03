package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SidisiBroodTyrant;
import com.github.laxika.magicalvibes.cards.w.WateryGrave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalustradeSpy.class, Divination.class, Forest.class, GrizzlyBears.class})
class BalustradeSpyTest extends BaseCardTest {

    private void castBalustradeSpy(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new BalustradeSpy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB mills target player until the first land, including that land")
    void millsTargetPlayerUntilFirstLand() {
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new Divination(),
                new Forest(),
                new GrizzlyBears()
        ));

        castBalustradeSpy(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Grizzly Bears", "Divination", "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name")
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can target its controller")
    void canTargetController() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(),
                new Forest(),
                new Divination()
        ));

        castBalustradeSpy(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting("name")
                .containsExactly("Divination");
    }

    @Test
    @CardUsed({WateryGrave.class})
    @DisplayName("A land on top is put into the graveyard without revealing the next card")
    void stopsAtLandOnTop() {
        var land = new WateryGrave();
        var remaining = new BalustradeSpy();
        harness.setLibrary(player2, List.of(land, remaining));

        castBalustradeSpy(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("A library without lands is put entirely into the graveyard")
    void putsEntireLandlessLibraryIntoGraveyard() {
        var first = new BalustradeSpy();
        var second = new BalustradeSpy();
        harness.setLibrary(player2, List.of(first, second));

        castBalustradeSpy(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Balustrade Spy");
    }

    @Test
    @DisplayName("An empty library reveals nothing and does not prevent the creature entering")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());

        castBalustradeSpy(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Balustrade Spy");
    }

    @Test
    @CardUsed({SidisiBroodTyrant.class, WateryGrave.class})
    @DisplayName("All revealed creature cards enter the graveyard as one event for Sidisi")
    void revealedCardsEnterGraveyardTogether() {
        harness.addToBattlefield(player2, new SidisiBroodTyrant());
        var first = new BalustradeSpy();
        var second = new BalustradeSpy();
        var land = new WateryGrave();
        harness.setLibrary(player2, List.of(first, second, land));

        castBalustradeSpy(player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second, land);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Zombie")))
                .hasSize(1);
    }
}
