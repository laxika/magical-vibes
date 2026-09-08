package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FanaticalFirebrandTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 1 damage to a player")
    void sacrificesAndDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyFirebrand(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Fanatical Firebrand");
        harness.assertInGraveyard(player1, "Fanatical Firebrand");

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToTargetCreature() {
        addReadyFirebrand(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 1 damage to a 1/1 target creature")
    void dealsDamageToOneToughnessCreature() {
        addReadyFirebrand(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent target = findPermanent(player2, "Llanowar Elves");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    private Permanent addReadyFirebrand(Player player) {
        Permanent permanent = new Permanent(new FanaticalFirebrand());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
