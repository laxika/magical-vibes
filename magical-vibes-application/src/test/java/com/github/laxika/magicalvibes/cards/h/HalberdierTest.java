package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PatrolHound;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Halberdier.class, PatrolHound.class})
class HalberdierTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Halberdier destroy a 2/2 blocker before it deals combat damage")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        addCreatureReady(player1, new Halberdier());
        addCreatureReady(player2, new PatrolHound());

        declareAttackers(List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Halberdier");
        harness.assertInGraveyard(player2, "Patrol Hound");
    }
}
