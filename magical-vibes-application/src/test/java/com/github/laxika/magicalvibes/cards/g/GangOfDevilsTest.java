package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GangOfDevilsTest extends BaseCardTest {

    /**
     * Kills a Gang of Devils that player1 controls with a Flame Javelin cast by player2, so the
     * death trigger goes on the stack. The divided damage assignments must already be staged.
     */
    private void killGangOfDevils() {
        harness.addToBattlefield(player1, new GangOfDevils());
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID gangId = harness.getPermanentId(player1, "Gang of Devils");
        harness.castInstant(player2, 0, gangId);
        harness.passBothPriorities(); // Flame Javelin resolves → Gang dies → death trigger on stack
        harness.passBothPriorities(); // death trigger resolves
    }

    @Test
    @DisplayName("Death trigger deals all 3 damage to a single creature")
    void deathDeals3DamageToSingleCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        gd.pendingETBDamageAssignments = Map.of(bearsId, 3);

        killGangOfDevils();

        harness.assertInGraveyard(player1, "Gang of Devils");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger deals all 3 damage to a player")
    void deathDeals3DamageToPlayer() {
        harness.setLife(player2, 20);

        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);

        killGangOfDevils();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Death trigger divides damage among three targets")
    void deathDividesDamageAmongThreeTargets() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        gd.pendingETBDamageAssignments = Map.of(
                bearsId, 1,
                player1.getId(), 1,
                player2.getId(), 1);

        killGangOfDevils();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Death trigger with no assignments deals no damage")
    void deathWithNoAssignments() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        gd.pendingETBDamageAssignments = Map.of();

        killGangOfDevils();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
