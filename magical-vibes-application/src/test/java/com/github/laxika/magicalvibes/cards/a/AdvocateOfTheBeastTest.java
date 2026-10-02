package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.m.MaraudingMaulhorn;
import com.github.laxika.magicalvibes.cards.s.ScavengingOoze;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdvocateOfTheBeast.class, MaraudingMaulhorn.class, ScavengingOoze.class, DoomBlade.class})
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
        Permanent nonBeast = harness.addToBattlefieldAndReturn(player1, new ScavengingOoze());
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
        harness.addToBattlefield(player1, new ScavengingOoze());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger at the opponent's end step")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new AdvocateOfTheBeast());
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new MaraudingMaulhorn());

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger still resolves after Advocate is destroyed")
    void resolvesAfterSourceIsDestroyed() {
        Permanent advocate = harness.addToBattlefieldAndReturn(player1, new AdvocateOfTheBeast());
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new MaraudingMaulhorn());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, beast.getId());
        harness.castInstant(player1, 0, advocate.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Advocate of the Beast");
        harness.passBothPriorities();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on another Beast when the chosen target is destroyed")
    void doesNotRetargetAfterTargetIsDestroyed() {
        harness.addToBattlefield(player1, new AdvocateOfTheBeast());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MaraudingMaulhorn());
        Permanent otherBeast = harness.addToBattlefieldAndReturn(player1, new MaraudingMaulhorn());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Marauding Maulhorn");
        harness.passBothPriorities();

        assertThat(otherBeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
