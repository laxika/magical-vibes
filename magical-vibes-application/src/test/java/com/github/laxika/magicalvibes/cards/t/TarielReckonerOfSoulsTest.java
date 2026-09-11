package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        addReadyTariel();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.stolenCreatures).containsKey(findBattlefieldPermanent(player1, "Grizzly Bears").getId());
    }

    @Test
    @DisplayName("Chooses exactly one creature when several are in the targeted opponent's graveyard")
    void returnsOnlyOneCreatureAtRandom() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addReadyTariel();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Grizzly Bears")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing when the targeted opponent has no creature cards in their graveyard")
    void noCreatureInTargetGraveyardDoesNothing() {
        harness.setGraveyard(player2, List.of(new Island()));
        addReadyTariel();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        addReadyTariel();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private Permanent findBattlefieldPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow();
    }

    private void addReadyTariel() {
        Permanent tariel = harness.addToBattlefieldAndReturn(player1, new TarielReckonerOfSouls());
        tariel.setSummoningSick(false);
    }
}
