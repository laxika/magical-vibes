package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaraudingMaulhorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdvocateOfTheBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the chosen Beast at the controller's end step")
    void putsCounterOnBeast() {
        harness.addToBattlefield(player1, new AdvocateOfTheBeast());
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new MaraudingMaulhorn());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, beast.getId());
        harness.passBothPriorities();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only Beasts you control are legal targets")
    void onlyControlledBeastsAreLegalTargets() {
        harness.addToBattlefield(player1, new AdvocateOfTheBeast());
        Permanent ownBeast = harness.addToBattlefieldAndReturn(player1, new MaraudingMaulhorn());
        Permanent nonBeast = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBeast = harness.addToBattlefieldAndReturn(player2, new MaraudingMaulhorn());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownBeast.getId())
                .doesNotContain(nonBeast.getId(), opponentBeast.getId());
    }

    @Test
    @DisplayName("No trigger target selection happens with no Beast on the battlefield")
    void noBeastNoTargeting() {
        harness.addToBattlefield(player1, new AdvocateOfTheBeast());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
