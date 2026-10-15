package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.v.VanguardsShield;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrideGuardian.class, RuneclawBear.class, VanguardsShield.class})
class PrideGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking triggers a 3 life gain for its controller")
    void blockingGains3Life() {
        addCreatureReady(player2, new PrideGuardian());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);

        int startingLife = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Pride Guardian");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    @DisplayName("No life is gained when the Guardian does not block")
    void noBlockNoLifeGain() {
        addCreatureReady(player2, new PrideGuardian());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The block trigger still gains life after its source leaves the battlefield")
    void gainsLifeAfterGuardianLeavesBattlefield() {
        Permanent guardian = addCreatureReady(player2, new PrideGuardian());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        int startingLife = gd.playerLifeTotals.get(player2.getId());
        int opponentLife = gd.playerLifeTotals.get(player1.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife);
        gd.playerBattlefields.get(player2.getId()).remove(guardian);
        gd.playerGraveyards.get(player2.getId()).add(guardian.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife + 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(opponentLife);
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers only one life gain")
    void blockingMultipleCreaturesGainsOnly3Life() {
        Permanent guardian = addCreatureReady(player2, new PrideGuardian());
        Permanent shield = new Permanent(new VanguardsShield());
        shield.setAttachedTo(guardian.getId());
        gd.playerBattlefields.get(player2.getId()).add(shield);
        addCreatureReady(player1, new RuneclawBear()).setAttacking(true);
        addCreatureReady(player1, new RuneclawBear()).setAttacking(true);
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife + 3);
    }
}
