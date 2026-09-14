package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalculatingLich.class, GrizzlyBears.class})
class CalculatingLichTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature attacking an opponent makes that player lose 1 life")
    void creaturesAttackingOpponentCauseLifeLoss() {
        addCreatureReady(player1, new CalculatingLich());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature attacking the Lich controller does not trigger it")
    void creatureAttackingLichControllerDoesNotTrigger() {
        addCreatureReady(player1, new CalculatingLich());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
