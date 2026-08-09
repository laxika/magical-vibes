package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class LowlandBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage destroys the damaged creature at end of combat")
    void combatDamageDestroysAtEndOfCombat() {
        Permanent basilisk = addReady(player1, new LowlandBasilisk());
        basilisk.setAttacking(true);
        addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player1, "Lowland Basilisk");
    }

    @Test
    @DisplayName("Noncombat damage also destroys the damaged creature at the next end of combat")
    void noncombatDamageDestroysAtNextEndOfCombat() {
        Permanent basilisk = addReady(player1, new LowlandBasilisk());
        Permanent spider = addReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(basilisk.getId(), spider.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Damage from another creature you control does not trigger the Basilisk")
    void damageFromAnotherCreatureDoesNotTrigger() {
        addReady(player1, new LowlandBasilisk());
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
