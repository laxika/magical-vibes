package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZimoneParadoxSculptor.class, BearCub.class, GoldveinPick.class})
class ZimoneParadoxSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of two chosen creatures you control at combat")
    void putsCountersAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new ZimoneParadoxSculptor());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BearCub());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles every counter on up to two controlled creatures and artifacts")
    void doublesCountersOnControlledCreaturesAndArtifacts() {
        Permanent zimone = addCreatureReady(player1, new ZimoneParadoxSculptor());
        Permanent creature = addCreatureReady(player1, new BearCub());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.CHARGE, 3);
        artifact.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(zimone.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent with the activated ability")
    void activatedAbilityOnlyTargetsControlledCreatureOrArtifact() {
        addCreatureReady(player1, new ZimoneParadoxSculptor());
        Permanent opponentCreature = addCreatureReady(player2, new BearCub());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat trigger may choose no targets")
    void combatTriggerMayChooseNoTargets() {
        Permanent zimone = harness.addToBattlefieldAndReturn(player1, new ZimoneParadoxSculptor());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat trigger may target Zimone alone")
    void combatTriggerMayTargetZimoneAlone() {
        Permanent zimone = harness.addToBattlefieldAndReturn(player1, new ZimoneParadoxSculptor());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BearCub());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, zimone.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Combat trigger does not trigger during the opponent's turn")
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent zimone = harness.addToBattlefieldAndReturn(player1, new ZimoneParadoxSculptor());

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Combat trigger rejects opposing creatures and noncreature artifacts")
    void combatTriggerRejectsIllegalTargets() {
        harness.addToBattlefield(player1, new ZimoneParadoxSculptor());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Activated ability may choose no targets and still pays its tap cost")
    void activatedAbilityMayChooseNoTargets() {
        Permanent zimone = prepareActivatedAbility();
        zimone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(zimone.isTapped()).isTrue();
        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability can target Zimone alone and doubles keyword counters")
    void activatedAbilityMayTargetZimoneAlone() {
        Permanent zimone = prepareActivatedAbility();
        zimone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        zimone.setCounterCount(CounterType.FLYING, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(zimone.getId()));
        harness.passBothPriorities();

        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(zimone.getCounterCount(CounterType.FLYING)).isEqualTo(2);
        assertThat(zimone.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Activated ability rejects choosing the same target twice")
    void activatedAbilityRejectsDuplicateTargets() {
        Permanent zimone = prepareActivatedAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(zimone.getId(), zimone.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability skips a target that changes controller but doubles the legal target")
    void activatedAbilityRechecksControlOnResolution() {
        Permanent zimone = prepareActivatedAbility();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        zimone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        artifact.setCounterCount(CounterType.CHARGE, 3);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(zimone.getId(), artifact.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat trigger skips a target that changes controller but counters the legal target")
    void combatTriggerRechecksControlOnResolution() {
        Permanent zimone = harness.addToBattlefieldAndReturn(player1, new ZimoneParadoxSculptor());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BearCub());
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, zimone.getId());
        harness.handlePermanentChosen(player1, other.getId());

        gd.playerBattlefields.get(player1.getId()).remove(other);
        gd.playerBattlefields.get(player2.getId()).add(other);
        harness.passBothPriorities();

        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent prepareActivatedAbility() {
        Permanent zimone = addCreatureReady(player1, new ZimoneParadoxSculptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        return zimone;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
