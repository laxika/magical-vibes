package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BurnTheImpureTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target creature without infect, no damage to controller")
    void deals3DamageToCreatureWithoutInfect() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Grizzly Bears is 2/2, should die to 3 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // No damage to controller since Grizzly Bears doesn't have infect
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals 3 damage to infect creature and 3 damage to its controller")
    void deals3DamageToInfectCreatureAnd3ToController() {
        harness.addToBattlefield(player2, new Blightwidow());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Blightwidow");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Blightwidow is 2/3, survives 3 damage
        harness.assertOnBattlefield(player2, "Blightwidow");
        // Controller takes 3 damage because Blightwidow has infect
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Burn the Impure");
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Burn the Impure");
        // No damage to controller when spell fizzles
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
