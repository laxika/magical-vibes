package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HissingMiasma.class, GrizzlyBears.class})
class HissingMiasmaTest extends BaseCardTest {

    private void setUpAttack(int count) {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new HissingMiasma()));

        for (int i = 0; i < count; i++) {
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(attacker);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Each attacking creature makes its controller lose 1 life")
    void eachAttackerCausesLifeLoss() {
        setUpAttack(2);
        int startingLife = gd.getLife(player2.getId());

        gs.declareAttackers(gd, player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declaring no attackers produces no trigger")
    void noAttackersNoTrigger() {
        setUpAttack(1);

        gs.declareAttackers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
