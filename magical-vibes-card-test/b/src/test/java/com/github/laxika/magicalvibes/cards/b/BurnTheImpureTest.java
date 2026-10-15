package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.PlagueMyr;
import com.github.laxika.magicalvibes.cards.s.SpinEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurnTheImpure.class, Blightwidow.class, SpinEngine.class, PlagueMyr.class})
class BurnTheImpureTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target creature without infect, no damage to controller")
    void deals3DamageToCreatureWithoutInfect() {
        harness.addToBattlefield(player2, new SpinEngine());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Spin Engine");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Spin Engine has 1 toughness and dies to 3 damage.
        harness.assertNotOnBattlefield(player2, "Spin Engine");
        harness.assertInGraveyard(player2, "Spin Engine");
        // No damage to controller since Spin Engine doesn't have infect
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

        // Blightwidow has 4 toughness and survives 3 damage.
        harness.assertOnBattlefield(player2, "Blightwidow");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(3);
        // Controller takes 3 damage because Blightwidow has infect
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new SpinEngine());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Spin Engine");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Burn the Impure");
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new SpinEngine());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Spin Engine");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Burn the Impure");
        // No damage to controller when spell fizzles
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Lethal damage to an infect creature still damages its controller")
    void lethalDamageToInfectCreatureStillDamagesController() {
        harness.addToBattlefield(player2, new PlagueMyr());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Plague Myr"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Plague Myr");
        harness.assertInGraveyard(player2, "Plague Myr");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Targeting your own infect creature damages you")
    void ownInfectCreatureDamagesItsController() {
        harness.addToBattlefield(player1, new Blightwidow());
        harness.setHand(player1, List.of(new BurnTheImpure()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Blightwidow"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blightwidow");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }
}
