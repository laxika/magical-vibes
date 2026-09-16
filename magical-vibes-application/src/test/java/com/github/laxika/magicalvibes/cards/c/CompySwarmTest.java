package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CompySwarm.class)
class CompySwarmTest extends BaseCardTest {

    @Test
    void doesNotCreateACopyWhenNoCreatureDied() {
        harness.addToBattlefield(player1, new CompySwarm());

        resolveControllerEndStep();

        assertThat(tokenCopies()).isEmpty();
    }

    @Test
    void createsOneTappedCopyWhenACreatureDied() {
        harness.addToBattlefield(player1, new CompySwarm());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        resolveControllerEndStep();

        assertThat(tokenCopies()).hasSize(1);
        assertThat(tokenCopies().getFirst().isTapped()).isTrue();
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private java.util.List<Permanent> tokenCopies() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
