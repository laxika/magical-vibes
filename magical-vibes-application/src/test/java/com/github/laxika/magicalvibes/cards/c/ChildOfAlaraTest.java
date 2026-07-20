package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChildOfAlaraTest extends BaseCardTest {

    @Test
    @DisplayName("When Child of Alara dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new ChildOfAlara());
        Permanent blocker = setupCombatWhereChildDies();

        harness.passBothPriorities();          // enter combat damage — Child (trample) awaits its assignment
        assignChildDamageToBlocker(blocker);   // assign all 6 to the 7-toughness blocker → Child dies

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Child of Alara"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Child of Alara");
    }

    @Test
    @DisplayName("Resolving the death trigger destroys all nonland permanents but spares lands")
    void destroysAllNonlandPermanents() {
        harness.addToBattlefield(player1, new ChildOfAlara());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        Permanent blocker = setupCombatWhereChildDies();

        harness.passBothPriorities();          // enter combat damage — Child (trample) awaits its assignment
        assignChildDamageToBlocker(blocker);   // assign all 6 to the 7-toughness blocker → Child dies
        harness.passBothPriorities();          // resolve death trigger — mass destruction

        // Every nonland permanent is gone on both battlefields; only lands remain.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(p -> p.getCard().hasType(CardType.LAND))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(p -> p.getCard().hasType(CardType.LAND))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND));

        // The other creatures were destroyed and put into their owners' graveyards.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    /**
     * Child of Alara (6/6 trample) attacks and is blocked by a 7/7 that deals it lethal damage while
     * surviving combat itself — so the blocker is still on the battlefield when the death trigger's
     * wipe resolves. Returns the blocker so the caller can supply Child's trample damage assignment.
     */
    private Permanent setupCombatWhereChildDies() {
        Permanent childPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Child of Alara"))
                .findFirst().orElseThrow();
        childPerm.setSummoningSick(false);
        childPerm.setAttacking(true);

        GrizzlyBears blocker = new GrizzlyBears();
        blocker.setPower(7);
        blocker.setToughness(7);
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return blockerPerm;
    }

    /**
     * Child (trample) has only 6 power against a 7-toughness blocker, so all 6 must be assigned to the
     * blocker (nothing tramples over). Resolving the assignment deals combat damage and kills Child.
     */
    private void assignChildDamageToBlocker(Permanent blocker) {
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));
    }
}
