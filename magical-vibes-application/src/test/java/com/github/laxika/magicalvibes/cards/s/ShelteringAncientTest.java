package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShelteringAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep puts a +1/+1 counter on an opponent's creature")
    void paysCumulativeUpkeep() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(ancient.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }

    @Test
    @DisplayName("The cumulative upkeep target is chosen from opponent creatures only")
    void choosesOpponentCreature() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId(), otherOpponentCreature.getId())
                .doesNotContain(ownCreature.getId(), ancient.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(otherOpponentCreature.getId()));

        assertThat(otherOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each cumulative upkeep payment chooses its opponent creature separately")
    void cumulativeUpkeepChoosesCreatureForEachPayment() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(otherOpponentCreature.getId()));

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }

    @Test
    @DisplayName("Without an opponent creature Sheltering Ancient cannot pay cumulative upkeep")
    void noOpponentCreatureSacrifices() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ancient);
        harness.assertInGraveyard(player1, "Sheltering Ancient");
    }
}
