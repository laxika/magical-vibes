package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevengeOfRavens.class, GrizzlyBears.class})
class RevengeOfRavensTest extends BaseCardTest {

    private void setUpAttack(Permanent... attackers) {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new RevengeOfRavens()));

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
    @DisplayName("Attacking causes life loss and life gain")
    void attackerControllerLosesLifeAndRevengeControllerGainsLife() {
        setUpAttack(new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of(0));
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger resolves once for each attacking creature")
    void triggersOncePerAttackingCreature() {
        setUpAttack(new Permanent(new GrizzlyBears()), new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of(0, 1));
        resolveTopTrigger();
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declaring no attackers does not trigger Revenge of Ravens")
    void noAttackersNoTrigger() {
        setUpAttack(new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void resolveTopTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
