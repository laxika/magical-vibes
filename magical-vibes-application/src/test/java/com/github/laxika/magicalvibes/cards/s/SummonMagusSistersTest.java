package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonMagusSisters.class, GrizzlyBears.class})
class SummonMagusSistersTest extends BaseCardTest {

    @Test
    void chaptersRandomlyApplyEachModeWithTheCorrectTargets() {
        Set<String> modes = new HashSet<>();

        for (int i = 0; i < 120 && modes.size() < 3; i++) {
            Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonMagusSisters());
            Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            int ownCountersBefore = ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            int lifeBefore = gd.playerLifeTotals.get(player1.getId());

            advanceToNextChapter();

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            UUID targetId = choice.validIds().contains(ownCreature.getId())
                    ? ownCreature.getId() : opposingCreature.getId();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();

            if (ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - ownCountersBefore == 3) {
                modes.add("combine");
            }
            if (gd.playerLifeTotals.get(player1.getId()) - lifeBefore == 3) {
                modes.add("defense");
            }
            if (!gd.playerBattlefields.get(player2.getId()).contains(opposingCreature)) {
                modes.add("fight");
            }

            assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        }

        assertThat(modes).containsExactlyInAnyOrder("combine", "defense", "fight");
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
