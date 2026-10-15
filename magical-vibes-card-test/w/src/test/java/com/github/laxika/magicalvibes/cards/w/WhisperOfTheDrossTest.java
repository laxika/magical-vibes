package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
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

@CardUsed({WhisperOfTheDross.class, CopperLonglegs.class, PropheticPrism.class})
class WhisperOfTheDrossTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -1/-1 until end of turn, then proliferates")
    void debuffsTargetAndProliferates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castAndResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.handleMultiplePermanentsChosen(player1, List.of(countered.getId()));

        assertThat(countered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

        castAndResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new WhisperOfTheDross()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, prism.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Proliferates every existing counter kind on selected permanents and players")
    void proliferatesAllCounterKindsOnChosenObjects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        target.setCounterCount(CounterType.OIL, 2);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        unchosen.setCounterCount(CounterType.OIL, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        castAndResolve(target);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId(), player2.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(unchosen.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Can choose zero objects to proliferate while still debuffing the target")
    void mayDeclineToAddAnyCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        target.setCounterCount(CounterType.OIL, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castAndResolve(target);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(target.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        harness.assertInGraveyard(player1, "Whisper of the Dross");
    }

    @Test
    @DisplayName("Does not proliferate when its only target leaves before resolution")
    void illegalTargetPreventsProliferation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        countered.setCounterCount(CounterType.OIL, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);

        harness.passBothPriorities();

        assertThat(countered.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Whisper of the Dross");
    }

    @Test
    @DisplayName("Proliferating a +1/+1 counter can save the target from lethal damage")
    void proliferatesBeforeCheckingLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        target.setMarkedDamage(3);

        castAndResolve(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player1, "Copper Longlegs");
    }

    @Test
    @DisplayName("Still proliferates when the debuff reduces its target to zero toughness")
    void proliferatesBeforeTargetDiesFromZeroToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castAndResolve(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Copper Longlegs");
        harness.assertInGraveyard(player2, "Copper Longlegs");
        harness.assertInGraveyard(player1, "Whisper of the Dross");
    }

    private void castAndResolve(Permanent target) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WhisperOfTheDross()));
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
