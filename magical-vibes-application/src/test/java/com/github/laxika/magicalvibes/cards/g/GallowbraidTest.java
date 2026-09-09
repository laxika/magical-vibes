package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gallowbraid.class, GoblinGrenadiers.class})
class GallowbraidTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep costs 1 life per age counter")
    void paysCumulativeUpkeepInLife() {
        Permanent gallowbraid = harness.addToBattlefieldAndReturn(player1, new Gallowbraid());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gallowbraid.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Gallowbraid");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Third upkeep costs 3 life")
    void thirdUpkeepCostsThreeLife() {
        Permanent gallowbraid = harness.addToBattlefieldAndReturn(player1, new Gallowbraid());
        gallowbraid.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gallowbraid.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Gallowbraid");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Cumulative upkeep cannot be partially paid when life is insufficient")
    void insufficientLifeCannotPartiallyPayCumulativeUpkeep() {
        Permanent gallowbraid = harness.addToBattlefieldAndReturn(player1, new Gallowbraid());
        gallowbraid.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Gallowbraid");
        harness.assertInGraveyard(player1, "Gallowbraid");
        harness.assertLife(player1, 2);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Gallowbraid")
    void decliningUpkeepSacrifices() {
        harness.addToBattlefield(player1, new Gallowbraid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Gallowbraid");
        harness.assertInGraveyard(player1, "Gallowbraid");
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Gallowbraid's controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent gallowbraid = harness.addToBattlefieldAndReturn(player1, new Gallowbraid());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gallowbraid.getCounterCount(CounterType.AGE)).isZero();
        harness.assertOnBattlefield(player1, "Gallowbraid");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Gallowbraid());
        Permanent blocker = addCreatureReady(player2, new GoblinGrenadiers());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Gallowbraid");
        harness.assertInGraveyard(player2, "Goblin Grenadiers");
    }
}
