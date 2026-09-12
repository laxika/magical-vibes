package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Attrition;
import com.github.laxika.magicalvibes.cards.w.WildColos;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Attrition.class, BubblingBeebles.class, WildColos.class})
class BubblingBeeblesTest extends BaseCardTest {

    @Test
    @DisplayName("Bubbling Beebles can't be blocked when defending player controls an enchantment")
    void cantBeBlockedWhenDefenderControlsEnchantment() {
        harness.addToBattlefield(player2, new Attrition());
        Permanent blocker = addCreatureReady(player2, new WildColos());
        Permanent beebles = attackingBeebles();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(beebles)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Bubbling Beebles can be blocked when defending player controls no enchantments")
    void canBeBlockedWhenDefenderControlsNoEnchantment() {
        Permanent blocker = addCreatureReady(player2, new WildColos());
        Permanent beebles = attackingBeebles();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(beebles))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Bubbling Beebles can be blocked when only its controller controls an enchantment")
    void canBeBlockedWhenOnlyAttackerControlsEnchantment() {
        harness.addToBattlefield(player1, new Attrition());
        Permanent blocker = addCreatureReady(player2, new WildColos());
        Permanent beebles = attackingBeebles();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(beebles))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Bubbling Beebles deals 3 damage")
    void dealsDamageWhenUnblocked() {
        harness.setLife(player2, 20);
        attackingBeebles();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent attackingBeebles() {
        Permanent beebles = new Permanent(new BubblingBeebles());
        beebles.setSummoningSick(false);
        beebles.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(beebles);
        return beebles;
    }
}
