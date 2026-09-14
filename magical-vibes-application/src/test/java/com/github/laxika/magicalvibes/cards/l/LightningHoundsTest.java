package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({LightningHounds.class, FreshVolunteers.class})
class LightningHoundsTest extends BaseCardTest {

    @Test
    @DisplayName("First strike defeats a smaller blocker before regular combat damage")
    void firstStrikeDefeatsSmallerBlocker() {
        Permanent attacker = addCreatureReady(player1, new LightningHounds());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightning Hounds");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }
}
