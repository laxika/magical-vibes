package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrialAndError.class, Cancel.class, GrizzlyBears.class})
class TrialAndErrorTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetPlayer() {
        TrialAndError trialAndError = new TrialAndError();
        harness.setHand(player1, List.of(trialAndError));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void mayCopyWhenCountered() {
        TrialAndError trialAndError = new TrialAndError();
        harness.setHand(player1, List.of(trialAndError));
        harness.addMana(player1, ManaColor.RED, 2);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, trialAndError.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void mayCopyWhenItFizzles() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TrialAndError trialAndError = new TrialAndError();
        harness.setHand(player1, List.of(trialAndError));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }
}
