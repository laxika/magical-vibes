package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.k.KinsbaileBalloonist;
import com.github.laxika.magicalvibes.cards.w.WizenedCenn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PloverKnights.class, HillcomberGiant.class, KinsbaileBalloonist.class, WizenedCenn.class})
class PloverKnightsTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Plover Knights")
    void flyingPreventsGroundBlockers() {
        Permanent ploverKnights = addCreatureReady(player1, new PloverKnights());
        Permanent blocker = addCreatureReady(player2, new HillcomberGiant());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ploverKnights)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ploverKnights);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("First strike lets Plover Knights destroy an equal blocker before it deals damage")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        Permanent ploverKnights = addCreatureReady(player1, new PloverKnights());
        harness.addToBattlefield(player2, new WizenedCenn());
        Permanent blocker = addCreatureReady(player2, new KinsbaileBalloonist());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ploverKnights)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(ploverKnights))));

        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ploverKnights);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
