package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrippingDead.class, WallOfStone.class, GrizzlyBears.class, Shock.class})
class DrippingDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage destroys the damaged creature and prevents regeneration")
    void combatDamageDestroysDamagedCreatureWithoutRegeneration() {
        Permanent drippingDead = addCreatureReady(player1, new DrippingDead());
        Permanent wall = addCreatureReady(player2, new WallOfStone());
        wall.setRegenerationShield(1);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dripping Dead");
        harness.assertNotOnBattlefield(player2, "Wall of Stone");
        harness.assertInGraveyard(player2, "Wall of Stone");
    }

    @Test
    @DisplayName("Noncombat damage does not trigger the destruction ability")
    void noncombatDamageDoesNotDestroy() {
        addCreatureReady(player1, new DrippingDead());
        Permanent wall = addCreatureReady(player2, new WallOfStone());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Stone");
    }

    @Test
    @DisplayName("Dripping Dead cannot be declared as a blocker")
    void cannotBlock() {
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new DrippingDead());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
