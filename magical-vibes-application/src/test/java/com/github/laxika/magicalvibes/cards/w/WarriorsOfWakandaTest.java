package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarriorsOfWakanda.class, GrizzlyBears.class})
class WarriorsOfWakandaTest extends BaseCardTest {

    @Test
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WarriorsOfWakanda());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, gd.playerBattlefields.get(player1.getId()).get(0), Keyword.TRAMPLE))
                .isTrue();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
