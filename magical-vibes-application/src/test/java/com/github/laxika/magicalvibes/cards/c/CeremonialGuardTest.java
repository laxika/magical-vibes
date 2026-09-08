package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CeremonialGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking destroys Ceremonial Guard at end of combat")
    void attackingDestroysItAtEndOfCombat() {
        Permanent guard = addReadyGuard(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(guard);
        harness.assertInGraveyard(player1, "Ceremonial Guard");
    }

    @Test
    @DisplayName("Blocking destroys Ceremonial Guard at end of combat")
    void blockingDestroysItAtEndOfCombat() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent guard = addReadyGuard(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(guard);
        harness.assertInGraveyard(player2, "Ceremonial Guard");
    }

    private Permanent addReadyGuard(Player player) {
        return addReady(player, new CeremonialGuard());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
