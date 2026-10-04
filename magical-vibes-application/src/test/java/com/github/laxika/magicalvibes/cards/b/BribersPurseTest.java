package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({BribersPurse.class, AlpineGrizzly.class, Forest.class})
class BribersPurseTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with gem counters equal to the value of X")
    void entersWithXGemCounters() {
        harness.setHand(player1, List.of(new BribersPurse()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent purse = findPermanent(player1, "Briber's Purse");
        assertThat(purse.getCounterCount(CounterType.GEM)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a gem counter stops a creature from attacking or blocking this turn")
    void removesGemCounterAndLocksCreature() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent bears = addCreatureReady(player2, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(purse.getCounterCount(CounterType.GEM)).isZero();
        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("The creature lock expires at end of turn")
    void lockExpiresAtEndOfTurn() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent bears = addCreatureReady(player2, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot activate without a gem counter")
    void cannotActivateWithoutGemCounter() {
        addReadyPurse(player1);
        Permanent bears = addCreatureReady(player2, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting with X zero leaves the Purse on the battlefield without gem counters")
    void entersWithZeroGemCounters() {
        harness.setHand(player1, List.of(new BribersPurse()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Briber's Purse").getCounterCount(CounterType.GEM)).isZero();
    }

    @Test
    @DisplayName("Mana, tapping, and the gem counter are paid before resolution")
    void paysCostsImmediatelyAndCanTargetOwnCreature() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 2);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(purse.isTapped()).isTrue();
        assertThat(purse.getCounterCount(CounterType.GEM)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A tapped Purse cannot activate even with mana and gem counters")
    void cannotActivateWhileTapped() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        purse.tap();
        Permanent creature = addCreatureReady(player2, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(purse.getCounterCount(CounterType.GEM)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation needs one mana as well as a gem counter")
    void cannotActivateWithoutMana() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent creature = addCreatureReady(player2, new AlpineGrizzly());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(purse.getCounterCount(CounterType.GEM)).isEqualTo(1);
        assertThat(purse.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves and lasts for the turn after the Purse leaves")
    void sourceLeavingDoesNotPreventResolution() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent creature = addCreatureReady(player2, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(purse);
        gd.playerGraveyards.get(player1.getId()).add(purse.getCard());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An ability with a departed target does not refund costs or lock another creature")
    void targetLeavingDoesNotRefundCosts() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());
        Permanent other = addCreatureReady(player2, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(purse.isTapped()).isTrue();
        assertThat(purse.getCounterCount(CounterType.GEM)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThatCode(() -> declareAttack(other)).doesNotThrowAnyException();
    }

    private Permanent addReadyPurse(Player player) {
        Permanent purse = harness.addToBattlefieldAndReturn(player, new BribersPurse());
        purse.setSummoningSick(false);
        return purse;
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        declareAttackers(player2, List.of(index));
    }
}
