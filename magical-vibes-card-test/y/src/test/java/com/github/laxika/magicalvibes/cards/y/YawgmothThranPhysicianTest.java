package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.r.RangerCaptainOfEos;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YawgmothThranPhysician.class, MotherBear.class, SnowCoveredForest.class,
        RangerCaptainOfEos.class, UniversalAutomaton.class})
class YawgmothThranPhysicianTest extends BaseCardTest {

    @Test
    void cannotTargetItselfBecauseItIsHuman() {
        Permanent yawgmoth = addCreatureReady(player1, new YawgmothThranPhysician());
        addCreatureReady(player1, new MotherBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, yawgmoth.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Mother Bear");
    }

    @Test
    void cannotTargetOpposingYawgmoth() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        addCreatureReady(player1, new MotherBear());
        Permanent target = addCreatureReady(player2, new YawgmothThranPhysician());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void humanCannotBlock() {
        Permanent yawgmoth = addCreatureReady(player1, new YawgmothThranPhysician());
        yawgmoth.setAttacking(true);
        addCreatureReady(player2, new RangerCaptainOfEos());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void changelingCannotBlock() {
        Permanent yawgmoth = addCreatureReady(player1, new YawgmothThranPhysician());
        yawgmoth.setAttacking(true);
        addCreatureReady(player2, new UniversalAutomaton());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void nonhumanCanBlock() {
        Permanent yawgmoth = addCreatureReady(player1, new YawgmothThranPhysician());
        yawgmoth.setAttacking(true);
        Permanent bear = addCreatureReady(player2, new MotherBear());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bear.isBlocking()).isTrue();
    }

    @Test
    void preventsCombatDamageFromHuman() {
        Permanent human = addCreatureReady(player1, new RangerCaptainOfEos());
        human.setAttacking(true);
        Permanent yawgmoth = addCreatureReady(player2, new YawgmothThranPhysician());
        yawgmoth.setBlocking(true);
        yawgmoth.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(yawgmoth.getMarkedDamage()).isZero();
        assertThat(human.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void sacrificingTheChosenTargetPreventsTheDraw() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        Permanent bear = addCreatureReady(player1, new MotherBear());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MotherBear()));

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Mother Bear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferatesOpposingPermanentAndEveryCounterKindOnSelectedPlayer() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        Permanent bear = addCreatureReady(player2, new MotherBear());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId(), player2.getId()));

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void mayProliferateNothingEvenWhenCountersExist() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        Permanent bear = addCreatureReady(player2, new MotherBear());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mother Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferateCanChooseYawgmothAndAddsEveryExistingCounterKind() {
        Permanent yawgmoth = addCreatureReady(player1, new YawgmothThranPhysician());
        yawgmoth.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        yawgmoth.setCounterCount(CounterType.STUN, 1);
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(yawgmoth.getId()));

        assertThat(yawgmoth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(yawgmoth.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(yawgmoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Yawgmoth, Thran Physician");
    }

    @Test
    void cannotProliferateWithoutACardToDiscard() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotProliferateWithoutTwoBlackMana() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mother Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pays life, sacrifices another creature, puts a -1/-1 counter on the target, and draws")
    void firstAbilityDoesEverything() {
        Permanent yawgmoth = addCreatureReady(player1, new YawgmothThranPhysician());
        Permanent fodder = addCreatureReady(player1, new MotherBear());
        Permanent target = addCreatureReady(player2, new MotherBear());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new MotherBear()));

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mother Bear");
        harness.assertInHand(player1, "Mother Bear");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(yawgmoth);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
    }

    @Test
    @DisplayName("The counter target is optional")
    void firstAbilityCanChooseNoTarget() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        harness.addToBattlefield(player1, new MotherBear());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new MotherBear()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Mother Bear");
        harness.assertInHand(player1, "Mother Bear");
    }

    @Test
    @DisplayName("The proliferate ability discards a card and adds another counter")
    void proliferatesAfterDiscarding() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        Permanent target = addCreatureReady(player1, new MotherBear());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mother Bear");
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new YawgmothThranPhysician());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.addToBattlefield(player1, new MotherBear());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}

@CardUsed({YawgmothThranPhysician.class, MotherBear.class})
class Mh1YawgmothThranPhysicianTest extends BaseCardTest {

    @Test
    @DisplayName("Pays life and sacrifices another creature to put a -1/-1 counter and draw")
    void sacrificesCreaturePutsCounterAndDraws() {
        addReadyYawgmoth();
        addCreatureReady(player1, new MotherBear());
        Permanent target = addCreatureReady(player2, new MotherBear());
        int startingLife = gd.getLife(player1.getId());
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 1);
        harness.assertOnBattlefield(player1, "Yawgmoth, Thran Physician");
        harness.assertNotOnBattlefield(player1, "Mother Bear");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May omit the target and still draws a card")
    void mayOmitTarget() {
        addReadyYawgmoth();
        addCreatureReady(player1, new MotherBear());
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot sacrifice Yawgmoth itself")
    void cannotSacrificeItself() {
        addReadyYawgmoth();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding a card and paying two black mana proliferates")
    void discardAndPayManaProliferates() {
        addReadyYawgmoth();
        Permanent target = addCreatureReady(player1, new MotherBear());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyYawgmoth() {
        return addCreatureReady(player1, new YawgmothThranPhysician());
    }
}
