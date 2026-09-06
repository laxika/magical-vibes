package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TundraWolves.class, MonssGoblinRaiders.class})
class TundraWolvesTest extends BaseCardTest {

    @Test
    @DisplayName("First strike destroys a 1/1 blocker before it deals combat damage")
    void firstStrikeDealsDamageBeforeNonFirstStrikeCreature() {
        Permanent attacker = addCreatureReady(player1, new TundraWolves());
        Permanent blocker = addCreatureReady(player2, new MonssGoblinRaiders());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Mons's Goblin Raiders");
    }
}
