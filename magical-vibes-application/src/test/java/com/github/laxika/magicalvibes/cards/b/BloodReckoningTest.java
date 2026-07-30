package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodReckoningTest extends BaseCardTest {

    /** Puts Blood Reckoning on player1's battlefield and the given attackers on player2's. */
    private void setUpAttack(Permanent... attackers) {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new BloodReckoning()));

        for (Permanent attacker : attackers) {
            attacker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(attacker);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("An attacking creature's controller loses 1 life when the trigger resolves")
    void attackerControllerLosesOneLife() {
        setUpAttack(new Permanent(new GrizzlyBears()));
        int startingLife = gd.getLife(player2.getId());

        gs.declareAttackers(gd, player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The trigger fires once per attacking creature")
    void triggersOncePerAttacker() {
        setUpAttack(new Permanent(new GrizzlyBears()), new Permanent(new GrizzlyBears()));
        int startingLife = gd.getLife(player2.getId());

        gs.declareAttackers(gd, player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("Declaring no attackers produces no trigger and no life loss")
    void noAttackersNoTrigger() {
        setUpAttack(new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
