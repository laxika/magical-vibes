package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmissaryOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player loses life for each artifact they control")
    void losesLifePerArtifactControlledByDamagedPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair()).setAttacking(true);
        addPermanent(player1, new LeoninScimitar());
        addPermanent(player2, new LeoninScimitar());
        addPermanent(player2, new Ornithopter());
        addPermanent(player2, new GrizzlyBears());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Causes no extra life loss when the damaged player controls no artifacts")
    void noExtraLifeLossWithoutArtifacts() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair()).setAttacking(true);
        addPermanent(player2, new GrizzlyBears());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair()).setAttacking(true);
        addPermanent(player2, new LeoninScimitar());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void resolveCombatAndTrigger() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player,
                                   com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
