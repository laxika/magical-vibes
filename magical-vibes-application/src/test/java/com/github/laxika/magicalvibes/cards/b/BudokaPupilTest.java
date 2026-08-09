package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KodamasMight;
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

class BudokaPupilTest extends BaseCardTest {

    @Test
    @DisplayName("May put a ki counter on itself when an Arcane spell is cast")
    void arcaneSpellAddsKiCounterWhenAccepted() {
        Permanent pupil = addPupil(player1);
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pupil.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pupil.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flips at the end step when it has two ki counters and the choice is accepted")
    void flipsAtEndStepWithTwoKiCounters() {
        Permanent pupil = addPupil(player1);
        pupil.setCounterCount(CounterType.KI, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(pupil.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Ichiga removes a ki counter to give a target creature +2/+2")
    void ichigaBoostsTargetForKiCounter() {
        BudokaPupil card = new BudokaPupil();
        Permanent ichiga = new Permanent(card);
        ichiga.setCard(card.getBackFaceCard());
        ichiga.setTransformed(true);
        ichiga.setSummoningSick(false);
        ichiga.setCounterCount(CounterType.KI, 1);
        gd.playerBattlefields.get(player1.getId()).add(ichiga);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(ichiga.getCounterCount(CounterType.KI)).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent addPupil(Player player) {
        return addCreatureReady(player, new BudokaPupil());
    }
}
