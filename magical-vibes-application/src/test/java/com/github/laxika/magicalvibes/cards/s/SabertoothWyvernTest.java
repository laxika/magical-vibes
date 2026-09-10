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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }
}
