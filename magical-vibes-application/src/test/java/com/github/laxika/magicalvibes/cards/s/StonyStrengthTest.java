package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({StonyStrength.class, SauroformHybrid.class, ScrabblingClaws.class})
class StonyStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Stony Strength puts a +1/+1 counter on and untaps a creature you control")
    void addsCounterAndUntaps() {
        Permanent target = addTappedCreature(player1);
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
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new StonyStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ScrabblingClaws());
        harness.setHand(player1, List.of(new StonyStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An untapped creature can receive the counter")
    void addsCounterToUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the targeted creature receives a counter and untaps")
    void leavesOtherCreaturesUnchanged() {
        Permanent target = addTappedCreature(player1);
        Permanent other = addTappedCreature(player1);

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Neither effect happens if the target changes controller before resolution")
    void targetChangesController() {
        Permanent target = addTappedCreature(player1);
        harness.setHand(player1, List.of(new StonyStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof StonyStrength);
    }

    @Test
    @DisplayName("The spell does not affect another creature when its target leaves the battlefield")
    void targetLeavesBattlefield() {
        Permanent target = addTappedCreature(player1);
        Permanent other = addTappedCreature(player1);
        harness.setHand(player1, List.of(new StonyStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof StonyStrength);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new StonyStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = addCreatureReady(player, new SauroformHybrid());
        perm.tap();
        return perm;
    }
}
