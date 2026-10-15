package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CourtHomunculus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GwafaHazidProfiteer.class, CourtHomunculus.class})
class GwafaHazidProfiteerTest extends BaseCardTest {


    @Test
    @DisplayName("Activation puts a bribery counter on the target and its controller draws a card")
    void activationPutsBriberyCounterAndControllerDraws() {
        addReadyGwafa(player1);
        Permanent bears = addHomunculus(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CourtHomunculus()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities(); // resolve ability

        assertThat(bears.getCounterCount(CounterType.BRIBERY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addReadyGwafa(player1);
        Permanent ownBears = addHomunculus(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("A creature with a bribery counter can't attack (applies even to Gwafa's controller)")
    void briberyCounterCreatureCannotAttack() {
        addReadyGwafa(player1);
        Permanent attacker = addHomunculus(player1);
        attacker.setCounterCount(CounterType.BRIBERY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature without a bribery counter can still attack")
    void creatureWithoutCounterCanAttack() {
        addReadyGwafa(player1);
        Permanent attacker = addHomunculus(player2);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player2, List.of(index));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature with a bribery counter can't block")
    void briberyCounterCreatureCannotBlock() {
        addReadyGwafa(player1);
        Permanent attacker = addHomunculus(player1);
        attacker.setAttacking(true);

        Permanent blocker = addHomunculus(player2);
        blocker.setCounterCount(CounterType.BRIBERY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Gwafa at attacker-battlefield index 0; the attacking Homunculus is at index 1.
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void abilityStillResolvesAfterGwafaDies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent gwafa = addReadyGwafa(player1);
        Permanent target = addHomunculus(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CourtHomunculus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gwafa.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gwafa Hazid, Profiteer");
        assertThat(target.getCounterCount(CounterType.BRIBERY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));
        assertThat(target.isAttacking()).isTrue();
    }

    @Test
    void removedTargetDoesNotCauseItsControllerToDraw() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addReadyGwafa(player1);
        Permanent target = addHomunculus(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CourtHomunculus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        target.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void targetBecomingControlledByYouMakesAbilityIllegal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addReadyGwafa(player1);
        Permanent target = addHomunculus(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new CourtHomunculus()));
        harness.setLibrary(player2, List.of(new CourtHomunculus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.BRIBERY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void creatureAlreadyAttackingRemainsInCombatWhenBribed() {
        harness.setLife(player1, 20);
        addReadyGwafa(player1);
        Permanent target = addHomunculus(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CourtHomunculus()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.BRIBERY)).isEqualTo(1);
        assertThat(target.isAttacking()).isTrue();
        harness.resolveCombatDamage();
        harness.assertLife(player1, 19);
    }

    @Test
    void canBribeCreatureAgainAndItsControllerDrawsAgain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        Permanent gwafa = addReadyGwafa(player1);
        Permanent target = addHomunculus(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CourtHomunculus(), new CourtHomunculus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gwafa.isTapped()).isTrue();
        harness.passBothPriorities();

        gwafa.untap();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.BRIBERY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void otherCounterTypesDoNotPreventAttacking() {
        addReadyGwafa(player1);
        Permanent attacker = addHomunculus(player2);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }

    private Permanent addReadyGwafa(Player player) {
        return addCreatureReady(player, new GwafaHazidProfiteer());
    }

    private Permanent addHomunculus(Player controller) {
        return addCreatureReady(controller, new CourtHomunculus());
    }
}
