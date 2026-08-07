package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MongrelPackTest extends BaseCardTest {

    @Test
    @DisplayName("Dying in combat creates four 1/1 Dog tokens")
    void diesInCombatCreatesDogs() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new MongrelPack());
        pack.setSummoningSick(false);
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4 kills the 4/1 Pack

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mongrel Pack");
        assertThat(countPermanents(player1, "Dog")).isEqualTo(4);
        assertThat(findPermanents(player1, "Dog"))
                .allSatisfy(dog -> {
                    assertThat(dog.getCard().getPower()).isEqualTo(1);
                    assertThat(dog.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Dying outside combat creates no tokens")
    void diesOutsideCombatCreatesNoDogs() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new MongrelPack());

        pack.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mongrel Pack");
        assertThat(countPermanents(player1, "Dog")).isZero();
    }
}
