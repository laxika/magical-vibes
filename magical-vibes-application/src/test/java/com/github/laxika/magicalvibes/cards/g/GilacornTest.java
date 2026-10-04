package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gilacorn.class, ColossalDreadmaw.class})
class GilacornTest extends BaseCardTest {

    @Test
    void deathtouchKillsLargerCreatureInCombat() {
        Permanent gilacorn = addCreatureReady(player1, new Gilacorn());
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());
        gilacorn.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gilacorn);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gilacorn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void deathtouchKillsLargerAttackerWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent gilacorn = addCreatureReady(player2, new Gilacorn());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gilacorn);
        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Gilacorn");
    }

    @Test
    void zeroPowerDoesNotDestroyBlockerWithDeathtouch() {
        Permanent gilacorn = addCreatureReady(player1, new Gilacorn());
        gilacorn.setPowerModifier(-1);
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());
        gilacorn.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gilacorn);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertNotInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    void unblockedDeathtouchCreatureOnlyDealsItsPowerToPlayer() {
        addCreatureReady(player1, new Gilacorn());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Gilacorn");
    }
}
