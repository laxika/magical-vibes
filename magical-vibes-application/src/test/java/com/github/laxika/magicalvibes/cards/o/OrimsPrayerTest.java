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

    @Test
    @DisplayName("Counts only creatures still attacking when the trigger resolves")
    void countsRemainingAttackersAtResolution() {
        harness.addToBattlefield(player1, new OrimsPrayer());
        setUpAttack(3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(0, 1, 2));
        Permanent removedAttacker = gd.playerBattlefields.get(player2.getId()).removeFirst();
        gd.playerGraveyards.get(player2.getId()).add(removedAttacker.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Gains no life if all attackers leave before resolution")
    void gainsNoLifeWhenAllAttackersLeave() {
        harness.addToBattlefield(player1, new OrimsPrayer());
        setUpAttack(1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(0));
        Permanent removedAttacker = gd.playerBattlefields.get(player2.getId()).removeFirst();
        gd.playerGraveyards.get(player2.getId()).add(removedAttacker.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("The trigger resolves after Orim's Prayer leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent prayer = harness.addToBattlefieldAndReturn(player1, new OrimsPrayer());
        setUpAttack(2);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(prayer);
        gd.playerGraveyards.get(player1.getId()).add(prayer.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Each copy triggers once for the attacking group")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new OrimsPrayer());
        harness.addToBattlefield(player1, new OrimsPrayer());
        setUpAttack(2);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
        assertThat(gd.stack).isEmpty();
    }
}
