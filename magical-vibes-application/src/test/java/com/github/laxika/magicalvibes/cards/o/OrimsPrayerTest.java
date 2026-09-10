package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrimsPrayer.class, LowlandGiant.class})
class OrimsPrayerTest extends BaseCardTest {

    /** Puts {@code count} ready attackers on player2's battlefield. */
    private void setUpAttack(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new LowlandGiant());
        }
    }

    @Test
    @DisplayName("Gains 1 life for each attacking creature, from a single trigger")
    void gainsOneLifePerAttacker() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new OrimsPrayer()));
        setUpAttack(3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(0, 1, 2));

        // "Whenever one or more creatures attack you" triggers once, not once per attacker
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    @DisplayName("Does not trigger when its controller is not attacked")
    void doesNotTriggerWhenControllerNotAttacked() {
        setUpAttack(2);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new OrimsPrayer()));
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife);
    }
}
