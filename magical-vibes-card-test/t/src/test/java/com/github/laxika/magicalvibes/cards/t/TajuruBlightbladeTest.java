package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruBlightblade.class, CanopyBaloth.class})
class TajuruBlightbladeTest extends BaseCardTest {

    @Test
    void deathtouchDestroysLargerAttackerWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new CanopyBaloth());
        Permanent blightblade = addCreatureReady(player2, new TajuruBlightblade());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blightblade);
        harness.assertInGraveyard(player1, "Canopy Baloth");
        harness.assertInGraveyard(player2, "Tajuru Blightblade");
    }

    @Test
    void deathtouchDestroysLargerBlocker() {
        Permanent blightblade = addCreatureReady(player1, new TajuruBlightblade());
        Permanent blocker = addCreatureReady(player2, new CanopyBaloth());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(blightblade)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(blightblade))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blightblade);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Tajuru Blightblade");
        harness.assertInGraveyard(player2, "Canopy Baloth");
    }

    @Test
    void deathtouchDealsNormalDamageToPlayer() {
        addCreatureReady(player1, new TajuruBlightblade());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Tajuru Blightblade");
    }
}
