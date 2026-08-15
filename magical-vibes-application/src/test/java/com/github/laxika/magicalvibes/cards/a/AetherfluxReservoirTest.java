package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class AetherfluxReservoirTest extends BaseCardTest {

    @Test
    @DisplayName("The spell-cast triggers count spells when they resolve")
    void spellCastTriggersCountSpellsAtResolution() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.setLife(player1, 20);

        harness.castArtifact(player1, 0);
        harness.castArtifact(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Paying 50 life deals 50 damage to a player")
    void paysLifeToDealDamage() {
        harness.addToBattlefield(player1, new AetherfluxReservoir());
        harness.setLife(player1, 60);
        harness.setLife(player2, 60);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }
}
