package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoviceDissectorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on the target creature")
    void sacrificesAnotherCreatureAndPutsCounterOnTarget() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()))
                .anyMatch(permanent -> permanent.getId().equals(dissector.getId()));
    }

    @Test
    @DisplayName("Cannot sacrifice Novice Dissector itself")
    void cannotSacrificeItself() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dissector.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new NoviceDissector());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        addCreatureReady(player1, new NoviceDissector());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
