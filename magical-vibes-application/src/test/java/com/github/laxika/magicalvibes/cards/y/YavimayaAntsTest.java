package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AgentOfStromgald;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaAnts.class, AgentOfStromgald.class})
class YavimayaAntsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Yavimaya Ants")
    void paysCumulativeUpkeep() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(ants.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ants);
    }

    @Test
    void cumulativeUpkeepCostScalesWithAgeCounters() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ants.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ants);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during its controller's upkeep")
    void triggersOnlyDuringItsControllersUpkeep() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ants.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ants);
    }

    @Test
    @DisplayName("Colorless mana cannot pay green cumulative upkeep")
    void requiresGreenMana() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ants);
        harness.assertInGraveyard(player1, "Yavimaya Ants");
    }

    @Test
    @DisplayName("Haste allows Yavimaya Ants to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        declareAttackers(List.of(0));

        assertThat(ants.isAttackedThisTurn()).isTrue();
    }

    @Test
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new YavimayaAnts());
        Permanent blocker = addCreatureReady(player2, new AgentOfStromgald());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

       assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
       assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
   }

    @Test
    void partialCumulativeUpkeepPaymentSacrifices() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ants);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Yavimaya Ants")
    void declineSacrifices() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ants);
        harness.assertInGraveyard(player1, "Yavimaya Ants");
    }
}
