package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurntimberGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gives a target creature +1/+1 until end of turn")
    void entersTappedAndBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurntimberGrove()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent grove = findPermanent(player1, "Turntimber Grove");
        assertThat(grove.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurntimberGrove()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping adds one green mana")
    void tappingAddsGreenMana() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new TurntimberGrove());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
