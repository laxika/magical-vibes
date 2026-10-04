package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TwistedSpiderClone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrendelSpawnOfKnull.class, TwistedSpiderClone.class, GreenGoblinNemesis.class})
class GrendelSpawnOfKnullTest extends BaseCardTest {

    @Test
    @DisplayName("Deals three combat damage when unblocked")
    void dealsCombatDamageWhenUnblocked() {
        addCreatureReady(player1, new GrendelSpawnOfKnull());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent grendel = addCreatureReady(player1, new GrendelSpawnOfKnull());
        addCreatureReady(player2, new TwistedSpiderClone());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(grendel.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A flying creature can block Grendel")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new GrendelSpawnOfKnull());
        addCreatureReady(player2, new GreenGoblinNemesis());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deathtouch destroys a four-toughness attacker after only three damage")
    void deathtouchDestroysLargerAttacker() {
        addCreatureReady(player1, new TwistedSpiderClone());
        addCreatureReady(player2, new GrendelSpawnOfKnull());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TwistedSpiderClone);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrendelSpawnOfKnull);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
