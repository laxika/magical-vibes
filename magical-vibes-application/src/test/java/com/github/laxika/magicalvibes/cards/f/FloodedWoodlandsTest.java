package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.Bloodbriar;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodedWoodlands.class, BalduvianBears.class, BalduvianBarbarians.class, Forest.class, Island.class})
class FloodedWoodlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Green creature can't attack when its controller controls no land")
    void greenCreatureCannotAttackWithoutLand() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with a green creature sacrifices one land")
    void attackingGreenCreatureSacrificesOneLand() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(firstForest.getId()));

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    @Test
    @DisplayName("Two green attackers need two lands — one land isn't enough")
    void twoGreenAttackersNeedTwoLands() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("Two green attackers sacrifice two lands")
    void twoGreenAttackersSacrificeTwoLands() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0, 1));

        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("Non-green creatures attack freely and sacrifice nothing")
    void nonGreenCreatureAttacksWithoutSacrifice() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("A green creature can pay with any land, not only a Forest")
    void greenCreatureCanSacrificeAnyLand() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Island")).isZero();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("A nonattacking green creature does not add to the sacrifice cost")
    void onlyAttackingGreenCreaturesCount() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("A non-green creature can attack without a land")
    void nonGreenCreatureAttacksWithoutLand() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBarbarians());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    @Test
    @DisplayName("Two Flooded Woodlands charge two lands for one green attacker")
    void stackedFloodedWoodlandsChargePerCopy() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("An opponent's land cannot pay the green creature's cost")
    void opponentLandCannotPayCost() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        harness.addToBattlefield(player2, new Forest());
        addCreatureReady(player1, new BalduvianBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller chooses which land pays the attack cost")
    void controllerChoosesLandForAttackCost() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new BalduvianBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(forest.getId(), island.getId());
    }

    @Test
    @CardUsed(Bloodbriar.class)
    @DisplayName("Sacrificing a land for the attack cost triggers sacrifice abilities")
    void attackCostSacrificeTriggersSacrificeAbilities() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        Permanent bloodbriar = addCreatureReady(player1, new Bloodbriar());
        addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
