package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdaptiveSnapjaw;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
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

@CardUsed({BurstOfStrength.class, AdaptiveSnapjaw.class, PropheticPrism.class})
class BurstOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Burst of Strength puts a +1/+1 counter on the target and untaps it")
    void addsCounterAndUntaps() {
        Permanent target = addTappedCreature(player1);
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent target = addTappedCreature(player2);
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The +1/+1 counter persists past end of turn")
    void counterPersists() {
        Permanent target = addTappedCreature(player1);
        cast(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An untapped creature still receives the counter")
    void addsCounterToUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Adds one counter without replacing existing counters")
    void addsToExistingCounters() {
        Permanent target = addTappedCreature(player1);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not affect another creature when the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = addTappedCreature(player1);
        Permanent other = addTappedCreature(player1);
        BurstOfStrength spell = new BurstOfStrength();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = addCreatureReady(player, new AdaptiveSnapjaw());
        perm.tap();
        return perm;
    }
}
