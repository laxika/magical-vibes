package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeleminPerformance.class, Divination.class, Forest.class, GrizzlyBears.class})
class TeleminPerformanceTest extends BaseCardTest {

    private void castTeleminPerformance() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new TeleminPerformance()));
        harness.addMana(player1, ManaColor.BLUE, 5); // {3}{U}{U}
        harness.castSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Noncreature cards revealed are milled and the creature is stolen under the caster's control")
    void millsNoncreaturesAndStealsCreature() {
        harness.setLibrary(player2, List.of(new Divination(), new Forest(), new GrizzlyBears()));

        castTeleminPerformance();
        harness.passBothPriorities();

        // The revealed creature enters under the caster's control.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // The noncreature cards revealed along the way go to the target player's graveyard.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Divination", "Forest");

        // Every revealed card left the library.
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library with no creature is entirely milled and nothing is stolen")
    void noCreatureMillsEntireLibrary() {
        harness.setLibrary(player2, List.of(new Divination(), new Forest()));

        castTeleminPerformance();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Divination", "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library reveals nothing and does not cause a draw loss")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());

        castTeleminPerformance();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Telemin Performance");
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Revealing stops at the first creature and leaves later cards in library order")
    void firstCreatureStopsRevealing() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest remainingLand = new Forest();
        GrizzlyBears remainingCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature, remainingLand, remainingCreature));

        castTeleminPerformance();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingLand, remainingCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard()).isSameAs(creature));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new TeleminPerformance()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
