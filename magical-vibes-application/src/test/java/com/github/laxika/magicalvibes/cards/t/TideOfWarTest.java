package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TideOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking sacrifices either every blocking creature (win) or every blocked creature (loss)")
    void coinFlipSacrificesExactlyOneSide() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new TideOfWar());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        boolean blockerSacrificed = countPermanents(player2, "Grizzly Bears") == 0
                && countPermanents(player1, "Hill Giant") == 1;
        boolean attackerSacrificed = countPermanents(player1, "Hill Giant") == 0
                && countPermanents(player2, "Grizzly Bears") == 1;

        assertThat(blockerSacrificed != attackerSacrificed)
                .as("exactly one of the blocking or blocked creature is sacrificed")
                .isTrue();
        if (blockerSacrificed) {
            harness.assertInGraveyard(player2, "Grizzly Bears");
        } else {
            harness.assertInGraveyard(player1, "Hill Giant");
        }
    }

    @Test
    @DisplayName("Every creature on the losing side is sacrificed, not just one")
    void sacrificesEveryCreatureOnTheLosingSide() {
        Permanent firstAttacker = addReady(player1, new HillGiant());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addReady(player1, new HillGiant());
        secondAttacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        addReady(player1, new TideOfWar());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        boolean blockersSacrificed = countPermanents(player2, "Grizzly Bears") == 0
                && countPermanents(player1, "Hill Giant") == 2;
        boolean blockedSacrificed = countPermanents(player1, "Hill Giant") == 0
                && countPermanents(player2, "Grizzly Bears") == 2;

        assertThat(blockersSacrificed != blockedSacrificed)
                .as("both blockers or both blocked attackers are sacrificed")
                .isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker survives either branch")
    void unblockedAttackerIsUntouched() {
        Permanent blocked = addReady(player1, new HillGiant());
        blocked.setAttacking(true);
        Permanent unblocked = addReady(player1, new GrizzlyBears());
        unblocked.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new TideOfWar());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declaring no blockers does not trigger Tide of War")
    void noBlockersNoTrigger() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new TideOfWar());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Tide of War"));
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tide of War triggers once per block declaration, however many creatures blocked")
    void triggersOnceRegardlessOfBlockerCount() {
        Permanent firstAttacker = addReady(player1, new HillGiant());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addReady(player1, new HillGiant());
        secondAttacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        addReady(player1, new TideOfWar());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(gd.stack.stream().filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Tide of War"))).hasSize(1);
    }

    @Test
    @DisplayName("Tide of War watches a combat neither of whose creatures its controller controls")
    void triggersForCombatItsControllerIsNotIn() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new TideOfWar());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Hill Giant") + countPermanents(player2, "Grizzly Bears"))
                .as("one of the two combatants is sacrificed")
                .isEqualTo(1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
