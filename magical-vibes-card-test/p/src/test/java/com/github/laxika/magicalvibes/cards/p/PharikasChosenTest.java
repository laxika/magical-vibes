package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({PharikasChosen.class, PensiveMinotaur.class})
class PharikasChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a creature it damages in combat")
    void deathtouchDestroysCreatureItDamagesInCombat() {
        Permanent chosen = addCreatureReady(player1, new PharikasChosen());
        chosen.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Pensive Minotaur");
    }

    @Test
    @DisplayName("Deathtouch destroys a larger attacker even when the Chosen dies simultaneously")
    void deathtouchDestroysAttackerWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new PensiveMinotaur());
        attacker.setAttacking(true);
        Permanent chosen = addCreatureReady(player2, new PharikasChosen());
        chosen.setBlocking(true);
        chosen.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Pensive Minotaur");
        harness.assertInGraveyard(player2, "Pharika's Chosen");
    }

    @Test
    @DisplayName("Deathtouch deals normal combat damage to a player")
    void deathtouchDoesNotDestroyPlayer() {
        harness.setLife(player2, 20);
        Permanent chosen = addCreatureReady(player1, new PharikasChosen());
        chosen.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Pharika's Chosen");
    }
}
