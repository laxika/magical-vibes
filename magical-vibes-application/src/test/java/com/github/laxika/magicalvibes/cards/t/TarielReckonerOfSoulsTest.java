package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TarielReckonerOfSouls.class, GrizzlyBears.class, Island.class})
class TarielReckonerOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the only creature from the targeted opponent's graveyard onto your battlefield")
    void returnsCreatureFromTargetOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature, new Island()));
        addCreatureReady(player1, new TarielReckonerOfSouls());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.stolenCreatures).containsKey(findPermanent(player1, "Grizzly Bears").getId());
    }

    @Test
    @DisplayName("Chooses exactly one creature when several are in the targeted opponent's graveyard")
    void returnsOnlyOneCreatureAtRandom() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new TarielReckonerOfSouls());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing when the targeted opponent has no creature cards in their graveyard")
    void noCreatureInTargetGraveyardDoesNothing() {
        harness.setGraveyard(player2, List.of(new Island()));
        addCreatureReady(player1, new TarielReckonerOfSouls());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        addCreatureReady(player1, new TarielReckonerOfSouls());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Selects from the graveyard as it exists on resolution, even if initially empty")
    void choosesCreatureAtResolution() {
        addCreatureReady(player1, new TarielReckonerOfSouls());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return a creature that left the graveyard before resolution")
    void graveyardEmptiedBeforeResolution() {
        addCreatureReady(player1, new TarielReckonerOfSouls());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability resolves after Tariel leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent tariel = addCreatureReady(player1, new TarielReckonerOfSouls());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tariel);
        harness.setGraveyard(player1, List.of(tariel.getCard()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tariel, Reckoner of Souls");
    }

    @Test
    @DisplayName("Activation taps Tariel as a cost before the creature enters untapped")
    void paysTapCostAndReturnsUntappedCreature() {
        Permanent tariel = addCreatureReady(player1, new TarielReckonerOfSouls());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(tariel.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Grizzly Bears").isSummoningSick()).isTrue();
    }
}
