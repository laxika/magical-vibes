package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemonicRisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 flying Demon token at end step with exactly one creature")
    void createsDemonWithExactlyOneCreature() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new GrizzlyBears());

        runToEndStep();

        Permanent demon = findDemon();
        assertThat(demon).isNotNull();
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not trigger at end step with no creatures")
    void noTriggerWithZeroCreatures() {
        harness.addToBattlefield(player1, new DemonicRising());

        runToEndStep();

        assertThat(findDemon()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at end step with two creatures")
    void noTriggerWithTwoCreatures() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        runToEndStep();

        assertThat(findDemon()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's creatures count toward the condition")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        runToEndStep();

        assertThat(findDemon()).isNotNull();
    }

    @Test
    @DisplayName("Does not trigger on the opponent's end step")
    void noTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new DemonicRising());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findDemon()).isNull();
    }

    private void runToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step, trigger goes on the stack
        harness.passBothPriorities(); // resolve the trigger
    }

    private Permanent findDemon() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Demon".equals(p.getCard().getName()))
                .findFirst()
                .orElse(null);
    }
}
