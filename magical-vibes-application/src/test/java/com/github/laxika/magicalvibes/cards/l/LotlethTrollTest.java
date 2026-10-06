package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LotlethTroll.class, GrizzlyBears.class, Mountain.class})
class LotlethTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card puts a +1/+1 counter on this creature")
    void discardCreaturePutsCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new LotlethTroll());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the discard ability with no creature card in hand")
    void cannotActivateWithoutCreatureInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new LotlethTroll());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the discard ability with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new LotlethTroll());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving the regeneration ability grants a regeneration shield")
    void regenerationGrantsShield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new LotlethTroll());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lotleth Troll").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves it from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent troll = addCreatureReady(player1, new LotlethTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Lotleth Troll");
        Permanent survivor = findPermanent(player1, "Lotleth Troll");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateRegenerationWithoutMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new LotlethTroll());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Discard is paid before the counter ability resolves")
    void discardIsAnActivationCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new LotlethTroll());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Troll can discard multiple creatures for counters")
    void repeatedDiscardWhileTappedAndSummoningSick() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new LotlethTroll());
        troll.setSummoningSick(true);
        troll.tap();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(troll.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Each black mana buys a separate shield without immediately regenerating")
    void repeatedRegenerationDoesNotTapOrRemoveDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new LotlethTroll());
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        troll.setMarkedDamage(1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(troll.getRegenerationShield()).isEqualTo(2);
        assertThat(troll.isTapped()).isFalse();
        assertThat(troll.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters increase the damage that can trample over a blocker")
    void discardedCreaturesIncreaseTrampleDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent troll = addCreatureReady(player1, new LotlethTroll());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Lotleth Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
