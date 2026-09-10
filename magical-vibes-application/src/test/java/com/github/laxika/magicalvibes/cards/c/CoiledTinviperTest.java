package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoiledTinviper.class, MoggConscripts.class})
class CoiledTinviperTest extends BaseCardTest {

    @Test
    @DisplayName("First strike defeats a 2/2 blocker before it can deal combat damage")
    void firstStrikeDealsDamageBeforeNonFirstStrikeCreature() {
        Permanent attacker = addCreatureReady(player1, new CoiledTinviper());
        Permanent blocker = addCreatureReady(player2, new MoggConscripts());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
