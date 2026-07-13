package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VexingShusherTest extends BaseCardTest {

    // ===== Activated ability makes a target spell uncounterable =====

    @Test
    @DisplayName("Ability makes a target spell can't be countered, so a counterspell fails")
    void abilityProtectsTargetSpellFromCounter() {
        VexingShusher shusher = new VexingShusher();
        harness.addToBattlefield(player1, shusher);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G} for the bears + {R/G} for the ability

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3); // {1}{U}{U}

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);

        // Player2 responds with Cancel targeting the Grizzly Bears spell.
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());

        // Player1 makes the Grizzly Bears spell uncounterable (resolves before Cancel).
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Cancel resolved but couldn't counter — Grizzly Bears entered the battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static "This spell can't be countered" =====

    @Test
    @DisplayName("Vexing Shusher itself can't be countered")
    void selfCannotBeCountered() {
        VexingShusher shusher = new VexingShusher();
        harness.setHand(player1, List.of(shusher));
        harness.addMana(player1, ManaColor.GREEN, 2); // {R/G}{R/G}

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, shusher.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vexing Shusher"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Ability requires a spell target =====

    @Test
    @DisplayName("Ability cannot be activated without a spell on the stack")
    void abilityRequiresSpellTarget() {
        VexingShusher shusher = new VexingShusher();
        harness.addToBattlefield(player1, shusher);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }
}
