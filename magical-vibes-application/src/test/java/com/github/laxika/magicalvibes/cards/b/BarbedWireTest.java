package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BarbedWireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the active player on each upkeep")
    void dealsDamageToActivePlayerOnEachUpkeep() {
        harness.addToBattlefield(player1, new BarbedWire());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevents the next damage dealt by its source")
    void preventsNextDamageDealtBySource() {
        Permanent wire = addReadyBarbedWire(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .noneMatch(shield -> shield.sourceId().equals(wire.getId()));
    }

    @Test
    @DisplayName("Only prevents damage from the artifact that was activated")
    void onlyPreventsDamageFromActivatedArtifact() {
        addReadyBarbedWire(player1);
        Permanent activatedWire = addReadyBarbedWire(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .noneMatch(shield -> shield.sourceId().equals(activatedWire.getId()));
    }

    private Permanent addReadyBarbedWire(Player player) {
        Permanent wire = harness.addToBattlefieldAndReturn(player, new BarbedWire());
        wire.setSummoningSick(false);
        return wire;
    }
}
