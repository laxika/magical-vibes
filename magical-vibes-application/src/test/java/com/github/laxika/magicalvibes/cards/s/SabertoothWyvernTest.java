package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CinderCrawler;
import com.github.laxika.magicalvibes.cards.w.WaywardSoul;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SabertoothWyvern.class, CinderCrawler.class, WaywardSoul.class})
class SabertoothWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Sabertooth Wyvern")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SabertoothWyvern());
        addCreatureReady(player2, new CinderCrawler());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("First strike lets Sabertooth Wyvern survive combat with an equal blocker")
    void firstStrikeLetsItSurviveEqualCombat() {
        Permanent attacker = addCreatureReady(player1, new SabertoothWyvern());
        Permanent blocker = addCreatureReady(player2, new WaywardSoul());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("First strike lets Sabertooth Wyvern kill an attacking flyer before it deals damage")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new WaywardSoul());
        Permanent blocker = addCreatureReady(player2, new SabertoothWyvern());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Two Sabertooth Wyverns deal first strike damage simultaneously and both die")
    void firstStrikersDealDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new SabertoothWyvern());
        Permanent blocker = addCreatureReady(player2, new SabertoothWyvern());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }
}
