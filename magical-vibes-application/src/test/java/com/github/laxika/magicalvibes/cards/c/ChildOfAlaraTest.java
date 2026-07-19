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

import static org.assertj.core.api.Assertions.assertThat;

class ChildOfAlaraTest extends BaseCardTest {

    @Test
    @DisplayName("When Child of Alara dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new ChildOfAlara());
        setupCombatWhereChildDies();

        harness.passBothPriorities(); // combat damage — Child of Alara dies

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
        setupCombatWhereChildDies();

        harness.passBothPriorities(); // combat damage — Child of Alara dies
        harness.passBothPriorities(); // resolve death trigger — mass destruction

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

    private void setupCombatWhereChildDies() {
        Permanent childPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Child of Alara"))
                .findFirst().orElseThrow();
        childPerm.setSummoningSick(false);
        childPerm.setAttacking(true);

        // A 7/7 blocker deals lethal to the 6/6 Child of Alara and survives combat,
        // so it is still on the battlefield when the death trigger's wipe resolves.
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
    }
}
