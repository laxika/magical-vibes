package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnarlfangVermin.class, CentaurCourser.class, HillGiant.class, GrizzlyBears.class})
class SnarlfangVerminTest extends BaseCardTest {

    @Test
    void combatDamageSuspectsCreatureAndPerpetuallyGrantsAbility() {
        addCreatureReady(player1, new SnarlfangVermin());
        Permanent centaur = addCreatureReady(player2, new CentaurCourser());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(centaur.isSuspected()).isTrue();

        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent secondHillGiant = addCreatureReady(player1, new HillGiant());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat(player2);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(
                hillGiant.getId(), 2, secondHillGiant.getId(), 1));
        resolveAllTriggers();

        assertThat(hillGiant.isSuspected()).isTrue();
    }

    @Test
    void suspectedOpposingCreatureDeathMakesItsControllerLoseLife() {
        harness.setGraveyard(player1, List.of(new SnarlfangVermin()));
        Permanent blocker = addCreatureReady(player1, new HillGiant());
        Permanent secondBlocker = addCreatureReady(player1, new HillGiant());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setSuspected(true);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat(player2);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(
                blocker.getId(), 1, secondBlocker.getId(), 1));
        resolveAllTriggers();

        assertThat(blocker.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
