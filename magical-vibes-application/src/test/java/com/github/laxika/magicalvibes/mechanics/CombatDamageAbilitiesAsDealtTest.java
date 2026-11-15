package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.l.LeylinePhantom;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The abilities combat damage triggers are the ones their objects have as the damage is dealt, as the layers leave
 * them (CR 603.2, 510.2), read so after the state-based actions the damage causes.
 */
class CombatDamageAbilitiesAsDealtTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that lost all its abilities doesn't trigger its own \"deals combat damage\" ability")
    void creatureThatLostItsAbilitiesDoesNotTrigger() {
        Permanent phantom = addCreatureReady(player1, new LeylinePhantom());
        phantom.setLosesAllAbilitiesUntilEndOfTurn(true);
        phantom.setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Leyline Phantom");
    }
}
