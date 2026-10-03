package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AtarkaBeastbreaker;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EchoesOfTheKinTree.class, AtarkaBeastbreaker.class, ColossodonYearling.class})
class EchoesOfTheKinTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability bolsters the creature with the least toughness")
    void activatesAndBolstersLeastToughnessCreature() {
        harness.addToBattlefield(player1, new EchoesOfTheKinTree());
        Permanent leastToughCreature = harness.addToBattlefieldAndReturn(player1, new AtarkaBeastbreaker());
        Permanent largerCreature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Activating the ability lets the controller choose among tied least-tough creatures")
    void choosesAmongTiedLeastToughnessCreatures() {
        harness.addToBattlefield(player1, new EchoesOfTheKinTree());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AtarkaBeastbreaker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AtarkaBeastbreaker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 1));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bolster ignores an opponent's creature with lower toughness")
    void ignoresOpponentsCreatures() {
        harness.addToBattlefield(player1, new EchoesOfTheKinTree());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AtarkaBeastbreaker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The ability can resolve with no creatures controlled")
    void resolvesWithNoCreatures() {
        harness.addToBattlefield(player1, new EchoesOfTheKinTree());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AtarkaBeastbreaker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bolster determines the least toughness when the ability resolves")
    void choosesCreatureAtResolution() {
        harness.addToBattlefield(player1, new EchoesOfTheKinTree());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new AtarkaBeastbreaker());
        harness.passBothPriorities();

        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly without tapping the enchantment")
    void activatesRepeatedly() {
        harness.addToBattlefield(player1, new EchoesOfTheKinTree());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AtarkaBeastbreaker());
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
