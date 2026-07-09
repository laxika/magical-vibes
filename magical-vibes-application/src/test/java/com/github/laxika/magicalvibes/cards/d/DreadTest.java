package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DreadTest extends BaseCardTest {

    @Test
    @DisplayName("Creature that deals combat damage to Dread's controller is destroyed")
    void damageSourceIsDestroyed() {
        harness.addToBattlefield(player2, new Dread());
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("When Dread is put into a graveyard it is shuffled into its owner's library")
    void putIntoGraveyardShufflesIntoLibrary() {
        harness.setLibrary(player2, new java.util.ArrayList<>());
        Permanent dread = harness.addToBattlefieldAndReturn(player2, new Dread());
        dread.setMarkedDamage(6);

        harness.runStateBasedActions();
        harness.passBothPriorities(); // trigger resolves

        harness.assertNotInGraveyard(player2, "Dread");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dread"));
    }
}
