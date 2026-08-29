package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZimoneParadoxSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of two chosen creatures you control at combat")
    void putsCountersAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new ZimoneParadoxSculptor());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = new Permanent(new DarksteelCitadel());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
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
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
