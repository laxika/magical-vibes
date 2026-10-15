package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SpawnOfThraxes.class, Mountain.class, GrizzlyBears.class})
class SpawnOfThraxesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage to a player equal to Mountains controlled")
    void dealsDamageToPlayerEqualToControlledMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castSpawnOfThraxes(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("ETB deals Mountain-count damage to a creature")
    void dealsDamageToCreatureEqualToControlledMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSpawnOfThraxes(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB counts only the controller's Mountains")
    void countsOnlyControllerMountains() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        castSpawnOfThraxes(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB counts Mountains when the trigger resolves")
    void countsMountainsAtResolution() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castSpawnOfThraxes(player2.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Mountain"));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB includes Mountains entering before the trigger resolves")
    void includesMountainsEnteringBeforeResolution() {
        harness.addToBattlefield(player1, new Mountain());
        castSpawnOfThraxes(player2.getId());

        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void canDamageItsController() {
        harness.addToBattlefield(player1, new Mountain());
        castSpawnOfThraxes(player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB still deals damage after Spawn of Thraxes leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new Mountain());
        castSpawnOfThraxes(player2.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Spawn of Thraxes"));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void castSpawnOfThraxes(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SpawnOfThraxes()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
