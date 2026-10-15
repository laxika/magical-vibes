package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SporolothAncient.class, FomoriNomad.class})
class SporolothAncientTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, puts a spore counter on itself")
    void upkeepTriggerAddsSporeCounter() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures you control can remove two spore counters to create a Saproling")
    void controlledCreatureUsesGrantedAbility() {
        harness.addToBattlefield(player1, new SporolothAncient());
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        creature.setCounterCount(CounterType.FUNGUS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isZero();
        List<Permanent> saprolings = controlledSaprolings();
        assertThat(saprolings).hasSize(1);

        Permanent saproling = saprolings.getFirst();
        assertThat(saproling.getCard().getPower()).isEqualTo(1);
        assertThat(saproling.getCard().getToughness()).isEqualTo(1);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Sporoloth Ancient can use the ability it grants")
    void sourceUsesGrantedAbility() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        ancient.setCounterCount(CounterType.FUNGUS, 2);

        harness.activateAbility(player1, battlefieldIndex(ancient), 0, null, null);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(controlledSaprolings()).hasSize(1);
    }

    @Test
    @DisplayName("The granted ability requires two spore counters")
    void requiresTwoSporeCounters() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        ancient.setCounterCount(CounterType.FUNGUS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ancient), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures controlled by an opponent do not gain the ability")
    void doesNotGrantAbilityToOpponentCreatures() {
        harness.addToBattlefield(player1, new SporolothAncient());
        Permanent opponentCreature = addCreatureReady(player2, new FomoriNomad());
        opponentCreature.setCounterCount(CounterType.FUNGUS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("An opponent's upkeep does not add spore counters")
    void opponentUpkeepDoesNotAddCounters() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Only Ancient receives the upkeep counter, not other controlled creatures")
    void upkeepDoesNotAddCountersToOtherCreatures() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick creature can pay the cost during an opponent's turn")
    void tappedSummoningSickCreatureCanActivateOnOpponentTurn() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        ancient.setCounterCount(CounterType.FUNGUS, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());
        creature.setSummoningSick(true);
        creature.tap();
        creature.setCounterCount(CounterType.FUNGUS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);

        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
        assertThat(controlledSaprolings()).isEmpty();

        harness.passBothPriorities();

        assertThat(controlledSaprolings()).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Spore counters on Ancient cannot pay another creature's activation cost")
    void cannotSpendCountersFromGrantingAncient() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        ancient.setCounterCount(CounterType.FUNGUS, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());
        creature.setCounterCount(CounterType.FUNGUS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(controlledSaprolings()).isEmpty();
    }

    @Test
    @DisplayName("An activated ability resolves after Ancient leaves, but new activations are unavailable")
    void grantedAbilityResolvesAfterAncientLeaves() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());
        creature.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(ancient);
        harness.passBothPriorities();

        assertThat(controlledSaprolings()).hasSize(1);
        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(creature.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private List<Permanent> controlledSaprolings() {
        return findPermanents(player1, "Saproling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SAPROLING))
                .toList();
    }
}
