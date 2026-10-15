package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdificeOfAuthority.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class EdificeOfAuthorityTest extends BaseCardTest {


    @Test
    @DisplayName("First ability adds a brick counter and stops the target from attacking this turn")
    void firstAbilityLocksAttackAndAddsBrickCounter() {
        Permanent edifice = addReadyEdifice(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(edifice.getCounterCount(CounterType.BRICK)).isEqualTo(1);
        assertThatThrownBy(() -> declareBearsAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("First ability's attack lock wears off at end of turn")
    void firstAbilityLockWearsOffAtEndOfTurn() {
        addReadyEdifice(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareBearsAttack(bears)).doesNotThrowAnyException();
    }


    @Test
    @DisplayName("Second ability can't be activated with fewer than three brick counters")
    void secondAbilityRequiresThreeBrickCounters() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brick counters");
    }

    @Test
    @DisplayName("Second ability activates with three brick counters")
    void secondAbilityActivatesWithThreeBrickCounters() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }


    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        Permanent bears = detainOwnCreature(new GrizzlyBears());

        assertThatThrownBy(() -> declareBearsAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't block")
    void detainedCreatureCannotBlock() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities (mana abilities included)")
    void detainedCreatureCannotActivateAbilities() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the ability controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent bears = detainOwnCreature(new GrizzlyBears());

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareBearsAttack(bears)).doesNotThrowAnyException();
    }


    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyEdifice(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }


    @Test
    @DisplayName("An illegal target prevents the first ability from adding a brick counter")
    void removedTargetPreventsBrickCounter() {
        Permanent edifice = addReadyEdifice(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, bears.getId());

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(edifice.getCounterCount(CounterType.BRICK)).isZero();
        assertThat(edifice.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First ability does not prevent activating the target's mana ability")
    void firstAbilityAllowsManaAbility() {
        addReadyEdifice(player1);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThatCode(() -> harness.tapPermanent(player1, 1)).doesNotThrowAnyException();
        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability still resolves after its source leaves the battlefield")
    void secondAbilityResolvesWithoutSource() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(edifice);
        gd.playerGraveyards.get(player1.getId()).add(edifice.getCard());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Brick counters are neither spent nor checked again during resolution")
    void secondAbilityDoesNotSpendOrRecheckCounters() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        assertThat(edifice.getCounterCount(CounterType.BRICK)).isEqualTo(3);
        edifice.setCounterCount(CounterType.BRICK, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareBearsAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Second ability persists through cleanup and the opponent's turn start")
    void secondAbilityPersistsUntilControllersTurn() {
        Permanent bears = detainOwnCreature(new GrizzlyBears());
        gd.expireEndOfTurnFloatingEffects();
        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareBearsAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addReadyEdifice(Player player) {
        return harness.addToBattlefieldAndReturn(player, new EdificeOfAuthority());
    }

    /** Detains a creature player1 controls via the second ability (three brick counters). */
    private Permanent detainOwnCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent creature = addCreatureReady(player1, card);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        return creature;
    }

    /** Attempts to declare the given player1 creature (battlefield index 1) as an attacker. */
    private void declareBearsAttack(Permanent creature) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        declareAttackers(player1, List.of(index));
    }
}
