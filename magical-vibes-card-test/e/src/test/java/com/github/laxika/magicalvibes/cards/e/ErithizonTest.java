package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Erithizon.class)
class ErithizonTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player chooses any creature for the attack trigger")
    void defendingPlayerChoosesAnyCreature() {
        Permanent attacker = addCreatureReady(player1, new Erithizon());
        Permanent chosenCreature = addCreatureReady(player1, new Erithizon());
        Permanent defendingCreature = addCreatureReady(player2, new Erithizon());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                attacker.getId(), chosenCreature.getId(), defendingCreature.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, chosenCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player2, chosenCreature.getId());
        harness.passBothPriorities();

        assertThat(chosenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(defendingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The defending player may choose the attacking Erithizon itself")
    void canTargetItself() {
        Permanent attacker = addCreatureReady(player1, new Erithizon());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player2, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The defending player may put the counter on their own creature")
    void canTargetDefendersCreature() {
        Permanent attacker = addCreatureReady(player1, new Erithizon());
        Permanent defender = addCreatureReady(player2, new Erithizon());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player2, defender.getId());
        resolveAllTriggers();

        assertThat(defender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The attack trigger resolves after Erithizon leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent attacker = addCreatureReady(player1, new Erithizon());
        Permanent defender = addCreatureReady(player2, new Erithizon());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player2, defender.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        resolveAllTriggers();

        assertThat(defender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target that leaves the battlefield receives no counter and is not replaced")
    void removedTargetDoesNotReceiveCounter() {
        Permanent attacker = addCreatureReady(player1, new Erithizon());
        Permanent defender = addCreatureReady(player2, new Erithizon());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player2, defender.getId());
        gd.playerBattlefields.get(player2.getId()).remove(defender);
        resolveAllTriggers();

        assertThat(defender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
