package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.ThunderousWrath;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GangOfDevils.class, ThunderousWrath.class, MoorlandInquisitor.class})
class GangOfDevilsTest extends BaseCardTest {

    /**
     * Kills a Gang of Devils that player1 controls with a Thunderous Wrath cast by player2, so the
     * death trigger goes on the stack. The divided damage assignments must already be staged.
     */
    private void killGangOfDevils() {
        harness.addToBattlefield(player1, new GangOfDevils());
        harness.setHand(player2, List.of(new ThunderousWrath()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID gangId = harness.getPermanentId(player1, "Gang of Devils");
        harness.castInstant(player2, 0, gangId);
        harness.passBothPriorities(); // Thunderous Wrath resolves and the death trigger goes on the stack
        harness.passBothPriorities(); // death trigger resolves
    }

    @Test
    @DisplayName("Death trigger deals all 3 damage to a single creature")
    void deathDeals3DamageToSingleCreature() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        UUID bearsId = harness.getPermanentId(player2, "Moorland Inquisitor");

        gd.pendingETBDamageAssignments = Map.of(bearsId, 3);

        killGangOfDevils();

        harness.assertInGraveyard(player1, "Gang of Devils");
        harness.assertInGraveyard(player2, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("Death trigger deals all 3 damage to a player")
    void deathDeals3DamageToPlayer() {
        harness.setLife(player2, 20);

        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);

        killGangOfDevils();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Death trigger divides damage among three targets")
    void deathDividesDamageAmongThreeTargets() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        UUID bearsId = harness.getPermanentId(player2, "Moorland Inquisitor");

        gd.pendingETBDamageAssignments = Map.of(
                bearsId, 1,
                player1.getId(), 1,
                player2.getId(), 1);

        killGangOfDevils();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Moorland Inquisitor"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Death trigger requires targets and a division before resolution")
    void deathRequiresTargetSelection() {
        harness.addToBattlefield(player1, new GangOfDevils());
        harness.setHand(player2, List.of(new ThunderousWrath()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Gang of Devils"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gang of Devils");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Death trigger divides damage between two targets")
    void deathDividesDamageAmongTwoTargets() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        UUID creatureId = harness.getPermanentId(player2, "Moorland Inquisitor");
        harness.setLife(player2, 20);
        gd.pendingETBDamageAssignments = Map.of(creatureId, 2, player2.getId(), 1);

        killGangOfDevils();

        harness.assertInGraveyard(player2, "Moorland Inquisitor");
        harness.assertLife(player2, 19);
    }
}
