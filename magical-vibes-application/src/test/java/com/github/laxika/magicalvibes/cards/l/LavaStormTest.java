package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LavaStormTest extends BaseCardTest {

    private void setUpCombat() {
        addCreatureReady(player1, new GrizzlyBears());   // attacker, 2/2
        addCreatureReady(player2, new GrizzlyBears());   // blocker, 2/2
        addCreatureReady(player2, new GiantSpider());    // untouched bystander, 2/4

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void castLavaStorm(int modeIndex) {
        harness.setHand(player1, List.of(new LavaStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, modeIndex, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Attacking mode deals 2 damage to each attacking creature only")
    void attackingModeHitsAttackers() {
        setUpCombat();

        castLavaStorm(0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Blocking mode deals 2 damage to each blocking creature only")
    void blockingModeHitsBlockers() {
        setUpCombat();

        castLavaStorm(1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Outside combat neither mode damages anything")
    void noCombatNoDamage() {
        addCreatureReady(player1, new GrizzlyBears());

        castLavaStorm(0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Lava Storm");
    }
}
