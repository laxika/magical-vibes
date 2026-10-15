package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Bloodbriar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Leviathan.class, Island.class, GrizzlyBears.class, Bloodbriar.class, Humble.class})
class LeviathanTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        Permanent leviathan = harness.enterBattlefieldAndReturn(player1, new Leviathan());

        assertThat(leviathan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent leviathan = addCreatureReady(player1, new Leviathan());
        leviathan.tap();

        harness.performUntapStep(player1);

        assertThat(leviathan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep: sacrificing two Islands untaps Leviathan")
    void upkeepSacrificeTwoIslandsUntaps() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, true);

        assertThat(leviathan.isTapped()).isFalse();
        assertThat(islandCount(player1)).isZero();
    }

    @Test
    @DisplayName("Upkeep: declining keeps Leviathan tapped and the Islands")
    void upkeepDeclineKeepsEverything() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, false);

        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep: accepting with only one Island sacrifices nothing and stays tapped")
    void upkeepAcceptWithOneIslandDoesNothing() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, true);

        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep: sacrificing two Islands leaves other permanents untouched")
    void upkeepSacrificeOnlyIslands() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, true);

        assertThat(leviathan.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grizzlyBears);
        assertThat(islandCount(player1)).isZero();
    }

    @Test
    @DisplayName("Upkeep: controller chooses which two Islands to sacrifice")
    void upkeepControllerChoosesIslandsToSacrifice() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent thirdIsland = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(firstIsland.getId(), secondIsland.getId(), thirdIsland.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstIsland.getId(), thirdIsland.getId()));

        assertThat(leviathan.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondIsland)
                .doesNotContain(firstIsland, thirdIsland);
    }
    @Test
    @DisplayName("Cannot attack when controlling fewer than two Islands")
    void cannotAttackWithoutTwoIslands() {
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking sacrifices two Islands")
    void attackingSacrificesTwoIslands() {
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, choice.validIds().subList(0, 2));

        assertThat(islandCount(player1)).isEqualTo(1);
        // Attack still went through (10/10 deals combat damage to the defender)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    @Test
    @DisplayName("Cannot attack using Islands controlled by the opponent")
    void cannotAttackUsingOpponentsIslands() {
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot declare two Leviathans when only two Islands can be sacrificed")
    void cannotDeclareTwoLeviathansWithOnlyTwoIslands() {
        addCreatureReady(player1, new Leviathan());
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can declare two Leviathans when four Islands can be sacrificed")
    void canDeclareTwoLeviathansWithFourIslands() {
        addCreatureReady(player1, new Leviathan());
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(player1, List.of(0, 1));

        assertThat(islandCount(player1)).isZero();
    }

    @Test
    @DisplayName("Trample deals excess combat damage through a blocker")
    void trampleDealsExcessCombatDamageThroughBlocker() {
        harness.setLife(player2, 20);
        Permanent leviathan = addCreatureReady(player1, new Leviathan());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 8));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(leviathan);
    }

    @Test
    @DisplayName("Islands sacrificed to attack trigger sacrifice abilities")
    void sacrificedIslandsTriggerSacrificeAbilities() {
        Permanent bloodbriar = addCreatureReady(player1, new Bloodbriar());
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
    @Test
    @DisplayName("Opponent's upkeep does not trigger Leviathan's untap ability")
    void opponentsUpkeepDoesNotTrigger() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep sacrifice cannot use the opponent's Islands")
    void upkeepCannotUseOpponentsIslands() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        resolveUpkeepMay(player1, true);

        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isEqualTo(1);
        assertThat(islandCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep sacrifice remains optional when Leviathan is already untapped")
    void upkeepCanSacrificeWhenAlreadyUntapped() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, true);

        assertThat(leviathan.isTapped()).isFalse();
        assertThat(islandCount(player1)).isZero();
    }

    @Test
    @DisplayName("Tapped Islands can pay Leviathan's attack cost")
    void tappedIslandsCanPayAttackCost() {
        Permanent leviathan = addCreatureReady(player1, new Leviathan());
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();

        declareAttackers(player1, List.of(0));

        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isZero();
    }

    @Test
    @DisplayName("Leviathan with no abilities can attack without Islands")
    void losingAbilitiesRemovesAttackCostWithoutIslands() {
        Permanent leviathan = addCreatureReady(player1, new Leviathan());
        castHumble(leviathan);

        declareAttackers(player1, List.of(0));

        assertThat(leviathan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Leviathan with no abilities does not sacrifice Islands to attack")
    void losingAbilitiesPreservesIslandsWhenAttacking() {
        Permanent leviathan = addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        castHumble(leviathan);

        declareAttackers(player1, List.of(0));

        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isEqualTo(2);
    }

    private void castHumble(Permanent target) {
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void resolveUpkeepMay(Player player, boolean accept) {
        advanceToUpkeep(player);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player, accept);
    }

    private int islandCount(Player player) {
        return (int) countPermanents(player, "Island");
    }
}
