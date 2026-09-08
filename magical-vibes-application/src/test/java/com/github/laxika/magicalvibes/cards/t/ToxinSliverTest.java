package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarbedSliver;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToxinSliver.class, BarbedSliver.class, GiantSpider.class})
class ToxinSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature dealt combat damage by Toxin Sliver")
    void destroysCreatureDealtCombatDamage() {
        Permanent toxin = addCreatureReady(player1, new ToxinSliver());
        toxin.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Grants the destroy trigger to other Slivers")
    void grantsDestroyTriggerToOtherSlivers() {
        addCreatureReady(player1, new ToxinSliver());
        Permanent sliver = addCreatureReady(player1, new BarbedSliver());
        sliver.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent toxin = addCreatureReady(player1, new ToxinSliver());
        toxin.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        blocker.setRegenerationShield(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
