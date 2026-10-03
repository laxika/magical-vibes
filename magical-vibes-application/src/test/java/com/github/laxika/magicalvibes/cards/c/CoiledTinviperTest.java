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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Two first strikers deal lethal combat damage to each other simultaneously")
    void firstStrikersDealDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new CoiledTinviper());
        Permanent blocker = addCreatureReady(player2, new CoiledTinviper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Coiled Tinviper");
        harness.assertInGraveyard(player2, "Coiled Tinviper");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked first striker deals combat damage only once")
    void unblockedFirstStrikerDoesNotDealRegularCombatDamage() {
        addCreatureReady(player1, new CoiledTinviper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
