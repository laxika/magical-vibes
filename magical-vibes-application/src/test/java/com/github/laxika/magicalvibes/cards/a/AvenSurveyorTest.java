package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvenSurveyorTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Aven Surveyor")
    void counterModePutsCounterOnItself() {
        cast(0, null);

        Permanent surveyor = findPermanent(player1, "Aven Surveyor");
        assertThat(surveyor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, surveyor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, surveyor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bounce mode returns the target creature to its owner's hand")
    void bounceModeReturnsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bounce mode rejects a noncreature target")
    void bounceModeRejectsNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.f.Forest());

        harness.setHand(player1, List.of(new AvenSurveyor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new AvenSurveyor()));
        addMana();
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
