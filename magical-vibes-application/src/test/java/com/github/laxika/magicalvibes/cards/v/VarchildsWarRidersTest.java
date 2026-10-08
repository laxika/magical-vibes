package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.cards.i.IvoryGargoyle;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VarchildsWarRiders.class, StormCrow.class, IvoryGargoyle.class})
class VarchildsWarRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep gives the opponent a Survivor token and keeps the War-Riders")
    void payingUpkeepGivesOpponentASurvivor() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(riders.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riders);
        assertThat(survivorCount(player2)).isEqualTo(1);
        assertThat(survivorCount(player1)).isZero();
    }

    @Test
    @DisplayName("Survivor tokens fall back to another set's printing when ALL has none")
    void survivorTokenFallsBackWhenSourceSetLacksPrinting() {
        CardPrintingRegistry.registerTokenImages("FALLBACK_SURV", Map.of(
                CardPrintingRegistry.buildTokenKey("Survivor", 1, 1, CardColor.RED),
                new CardPrintingRegistry.TokenImageData("tfallback", "1")));

        VarchildsWarRiders riders = new VarchildsWarRiders();
        riders.setSetCode("ALL");
        riders.setCollectorNumber("83");
        harness.addToBattlefieldAndReturn(player1, riders);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent survivor = findPermanent(player2, "Survivor");
        assertThat(survivor.getCard().getSetCode()).isEqualTo("tfallback");
        assertThat(survivor.getCard().getCollectorNumber()).isEqualTo("1");
    }

    @Test
    @DisplayName("A second age counter makes the opponent create a second Survivor token")
    void secondUpkeepCreatesTwoMoreSurvivors() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(riders.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riders);
        assertThat(survivorCount(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the War-Riders and creates no token")
    void decliningSacrifices() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(riders);
        harness.assertInGraveyard(player1, "Varchild's War-Riders");
        assertThat(survivorCount(player2)).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 1 grants +1/+1 until end of turn")
    void twoBlockersGivesPlusOne() {
        Permanent riders = addReadyRiders(player1);
        riders.setAttacking(true);
        addReadyBlocker(player2);
        addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(riders.getPowerModifier()).isEqualTo(1);
        assertThat(riders.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A single blocker grants no Rampage bonus")
    void oneBlockerGivesNothing() {
        Permanent riders = addReadyRiders(player1);
        riders.setAttacking(true);
        addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(riders.getPowerModifier()).isZero();
        assertThat(riders.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing the only blocker before Rampage resolves gives no bonus")
    void removingOnlyBlockerBeforeRampageResolvesGivesNoBonus() {
        Permanent riders = addReadyRiders(player1);
        riders.setAttacking(true);
        addReadyGargoyle(player2);
        harness.addMana(player2, ManaColor.WHITE, 5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Ivory Gargoyle");
        harness.passBothPriorities();

        assertThat(riders.getPowerModifier()).isZero();
        assertThat(riders.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The opponent's upkeep does not add an age counter or create Survivors")
    void opponentsUpkeepDoesNotTrigger() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(riders.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(survivorCount(player1)).isZero();
        assertThat(survivorCount(player2)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riders);
    }

    @Test
    @DisplayName("Declining the second upkeep leaves the previously created Survivor")
    void decliningSecondUpkeepCreatesNoAdditionalSurvivors() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(riders);
        harness.assertInGraveyard(player1, "Varchild's War-Riders");
        assertThat(survivorCount(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Three blockers grant +2/+2 from a single Rampage trigger")
    void threeBlockersGivePlusTwo() {
        Permanent riders = addReadyRiders(player1);
        riders.setAttacking(true);
        addReadyBlocker(player2);
        addReadyBlocker(player2);
        addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(riders.getPowerModifier()).isEqualTo(2);
        assertThat(riders.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rampage counts the blockers still present when its trigger resolves")
    void removingOneOfTwoBlockersBeforeRampageResolvesGivesNoBonus() {
        Permanent riders = addReadyRiders(player1);
        riders.setAttacking(true);
        addReadyGargoyle(player2);
        addReadyBlocker(player2);
        harness.addMana(player2, ManaColor.WHITE, 5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Ivory Gargoyle");
        harness.passBothPriorities();

        assertThat(riders.getPowerModifier()).isZero();
        assertThat(riders.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Trample deals excess damage through a single blocker")
    void trampleDealsExcessDamage() {
        addReadyRiders(player1);
        Permanent blocker = addCreatureReady(player2, new StormCrow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertOnBattlefield(player1, "Varchild's War-Riders");
    }

    @Test
    @DisplayName("Rampage's bonus wears off at the end of the turn")
    void rampageBonusExpiresAtEndOfTurn() {
        Permanent riders = addReadyRiders(player1);
        Permanent firstBlocker = addCreatureReady(player2, new StormCrow());
        Permanent secondBlocker = addCreatureReady(player2, new StormCrow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();
        assertThat(riders.getPowerModifier()).isEqualTo(1);
        assertThat(riders.getToughnessModifier()).isEqualTo(1);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 2
        ));
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riders);
        assertThat(riders.getPowerModifier()).isZero();
        assertThat(riders.getToughnessModifier()).isZero();
    }

    private long survivorCount(Player player) {
        return countPermanents(player, "Survivor");
    }

    private Permanent addReadyRiders(Player player) {
        return addCreatureReady(player, new VarchildsWarRiders());
    }

    private void addReadyBlocker(Player player) {
        addCreatureReady(player, new StormCrow());
    }

    private void addReadyGargoyle(Player player) {
        addCreatureReady(player, new IvoryGargoyle());
    }
}
