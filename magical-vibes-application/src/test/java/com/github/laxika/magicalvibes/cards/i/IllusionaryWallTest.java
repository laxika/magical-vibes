package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.PhantasmalMount;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusionaryWall.class, PhantasmalMount.class})
class IllusionaryWallTest extends BaseCardTest {

    @Test
    @DisplayName("Cumulative upkeep does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(wall.getCounterCount(CounterType.AGE)).isZero();
        harness.assertOnBattlefield(player1, "Illusionary Wall");
    }

    @Test
    @DisplayName("Cumulative upkeep may be declined even when blue mana is available")
    void declinesWithManaAvailable() {
        harness.addToBattlefield(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Illusionary Wall");
        harness.assertInGraveyard(player1, "Illusionary Wall");
    }

    @Test
    @DisplayName("Nonblue mana cannot pay Illusionary Wall's cumulative upkeep")
    void wrongColorCannotPayUpkeep() {
        harness.addToBattlefield(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 5);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Illusionary Wall");
        harness.assertInGraveyard(player1, "Illusionary Wall");
    }

    @Test
    @DisplayName("Defender prevents Illusionary Wall from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new IllusionaryWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Illusionary Wall blocks a flyer and kills it before it deals combat damage")
    void flyingBlockerDealsFirstStrikeDamage() {
        addCreatureReady(player1, new PhantasmalMount());
        Permanent wall = addCreatureReady(player2, new IllusionaryWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertInGraveyard(player1, "Phantasmal Mount");
        harness.assertOnBattlefield(player2, "Illusionary Wall");
        assertThat(wall.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusionary Wall")
    void paysCumulativeUpkeep() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
    }

    @Test
    @DisplayName("Cumulative upkeep costs two blue mana on the second upkeep")
    void cumulativeUpkeepCostIncreases() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Partial cumulative upkeep payment is not accepted")
    void partialPaymentSacrificesWithoutSpendingMana() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());
        wall.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player1, "Illusionary Wall");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusionary Wall")
    void declineSacrifices() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player1, "Illusionary Wall");
    }
}
