package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.f.FrostRaptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShelteringAncient.class, BorealCentaur.class, FrostRaptor.class})
class ShelteringAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep puts a +1/+1 counter on an opponent's creature")
    void paysCumulativeUpkeep() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(ancient.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }

    @Test
    @DisplayName("The cumulative upkeep recipient is chosen from opponent creatures only")
    void choosesOpponentCreature() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        Permanent otherOpponentCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

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
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        Permanent otherOpponentCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(otherOpponentCreature.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId()));

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Sheltering Ancient")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ancient);
        harness.assertInGraveyard(player1, "Sheltering Ancient");
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

    @Test
    @DisplayName("The same opponent creature can receive every cumulative upkeep counter")
    void canChooseSameCreatureForEveryPayment() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        ancient.setCounterCount(CounterType.AGE, 1);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ancient.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }

    @Test
    @DisplayName("A single opposing creature can pay upkeep for multiple age counters")
    void singleCreatureReceivesEntirePayment() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        ancient.setCounterCount(CounterType.AGE, 2);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ancient.getCounterCount(CounterType.AGE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }

    @Test
    @DisplayName("Shroud does not prevent a creature from receiving upkeep counters")
    void upkeepDoesNotTargetOpponentCreature() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new ShelteringAncient());
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new FrostRaptor());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToUpkeep(player1);
        gd.playerManaPools.get(player2.getId()).addSnowMana(ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.SHROUD)).isTrue();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(raptor.getId()));

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
    }
}
