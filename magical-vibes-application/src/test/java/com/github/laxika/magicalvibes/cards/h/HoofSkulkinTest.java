package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoofSkulkinTest extends BaseCardTest {

    private Permanent addSkulkin() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new HoofSkulkin());
        skulkin.setSummoningSick(false);
        return skulkin;
    }

    @Test
    @DisplayName("Gives a green creature +1/+1 until end of turn, then it wears off")
    void boostsGreenCreature() {
        addSkulkin();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2 green
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(greenCreature.getEffectivePower()).isEqualTo(3);
        assertThat(greenCreature.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(greenCreature.getEffectivePower()).isEqualTo(2);
        assertThat(greenCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        addSkulkin();
        harness.addToBattlefield(player1, new BlackKnight()); // black
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Black Knight");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
