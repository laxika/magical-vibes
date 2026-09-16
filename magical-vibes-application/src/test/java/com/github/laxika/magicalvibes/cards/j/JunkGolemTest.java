package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.b.BlessedOrator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JunkGolem.class, AvenFisher.class, BlessedOrator.class})
class JunkGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new JunkGolem(), "{4}");
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Junk Golem");

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying the upkeep cost removes a +1/+1 counter from Junk Golem")
    void payingUpkeepCostRemovesCounter() {
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Declining the upkeep cost sacrifices Junk Golem")
    void decliningUpkeepCostSacrifices() {
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golem);
        harness.assertInGraveyard(player1, "Junk Golem");
    }

    @Test
    @DisplayName("The upkeep trigger occurs only during its controller's upkeep")
    void upkeepTriggerOccursOnlyDuringControllersUpkeep() {
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player2);

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With no +1/+1 counters the upkeep trigger sacrifices Junk Golem")
    void noCountersSacrifices() {
        harness.enterBattlefieldAndReturn(player1, new BlessedOrator());
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golem);
    }

    @Test
    @DisplayName("The activated ability requires a discarded card and adds a counter")
    void discardAddsCounter() {
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new AvenFisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Aven Fisher");
    }

    @Test
    @DisplayName("The activated ability cannot be used without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability cannot be used without its mana cost")
    void cannotActivateWithoutMana() {
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new AvenFisher()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-+1/+1 counter cannot pay the upkeep cost")
    void otherCounterTypeDoesNotPay() {
        harness.enterBattlefieldAndReturn(player1, new BlessedOrator());
        Permanent golem = addCreatureReady(player1, new JunkGolem());
        golem.setCounterCount(CounterType.CHARGE, 1);
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golem);
    }

}
