package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({Schismotivate.class, GrizzlyBears.class, Mountain.class})
class SchismotivateTest extends BaseCardTest {

    @Test
    @DisplayName("Gives one target creature +4/+0 and another -4/-0")
    void appliesBothModifiers() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        harness.passBothPriorities();

        assertThat(boosted.getEffectivePower()).isEqualTo(6);
        assertThat(boosted.getEffectiveToughness()).isEqualTo(2);
        assertThat(weakened.getEffectivePower()).isEqualTo(-2);
        assertThat(weakened.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both modifiers wear off at cleanup")
    void modifiersWearOffAtCleanup() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent weakened = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepare();

        harness.castInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        harness.passBothPriorities();
        assertThat(boosted.getEffectivePower()).isEqualTo(6);
        assertThat(weakened.getEffectivePower()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(boosted.getEffectivePower()).isEqualTo(2);
        assertThat(weakened.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Still modifies the surviving target when the other target leaves")
    void resolvesWithOneLegalTarget() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(boosted);
        harness.passBothPriorities();

        assertThat(weakened.getEffectivePower()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepare() {
        harness.setHand(player1, List.of(new Schismotivate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
