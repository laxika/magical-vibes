package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.Bloodbriar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MoorFiend;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reclamation.class, MoorFiend.class, BalduvianBears.class, Forest.class, Island.class})
class ReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature can't attack when its controller controls no land")
    void blackCreatureCannotAttackWithoutLand() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with a black creature sacrifices one land")
    void attackingBlackCreatureSacrificesOneLand() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());
        var firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(firstForest.getId()));

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    @Test
    @DisplayName("Two black attackers need two lands — one land isn't enough")
    void twoBlackAttackersNeedTwoLands() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());
        addCreatureReady(player1, new MoorFiend());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("Two black attackers sacrifice two lands")
    void twoBlackAttackersSacrificeTwoLands() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());
        addCreatureReady(player1, new MoorFiend());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0, 1));

        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("Non-black creatures attack freely and sacrifice nothing")
    void nonBlackCreatureAttacksWithoutSacrifice() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("A black creature can sacrifice any land, not only a Forest")
    void blackCreatureCanSacrificeAnyLand() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Island")).isZero();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("A nonattacking black creature does not add to the sacrifice cost")
    void onlyAttackingBlackCreaturesCount() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());
        addCreatureReady(player1, new MoorFiend());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("An opponent's land cannot pay the black creature's cost")
    void opponentLandCannotPayCost() {
        harness.addToBattlefield(player2, new Reclamation());
        harness.addToBattlefield(player2, new Forest());
        addCreatureReady(player1, new MoorFiend());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Reclamations charge two lands for one black attacker")
    void stackedReclamationsChargePerCopy() {
        harness.addToBattlefield(player2, new Reclamation());
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("The controller chooses which land pays the attack cost")
    void controllerChoosesLandForAttackCost() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new MoorFiend());
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        var island = harness.addToBattlefieldAndReturn(player1, new Island());

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
        harness.addToBattlefield(player2, new Reclamation());
        var bloodbriar = addCreatureReady(player1, new Bloodbriar());
        addCreatureReady(player1, new MoorFiend());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
