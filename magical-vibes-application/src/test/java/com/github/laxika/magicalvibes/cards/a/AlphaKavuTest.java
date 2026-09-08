package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KavuClimber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlphaKavuTest extends BaseCardTest {

    private void addReadyAlphaKavu() {
        harness.addToBattlefield(player1, new AlphaKavu());
        findPermanent(player1, "Alpha Kavu").setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Ability gives target Kavu -1/+1 until end of turn")
    void weakensAndToughensTargetKavu() {
        addReadyAlphaKavu();
        harness.addToBattlefield(player1, new KavuClimber());

        UUID targetId = harness.getPermanentId(player1, "Kavu Climber");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Kavu Climber");
        assertThat(kavu.getEffectivePower()).isEqualTo(2);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addReadyAlphaKavu();
        harness.addToBattlefield(player1, new KavuClimber());

        UUID targetId = harness.getPermanentId(player1, "Kavu Climber");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Kavu Climber");
        assertThat(kavu.getEffectivePower()).isEqualTo(3);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability cannot target a non-Kavu creature")
    void rejectsNonKavuTarget() {
        addReadyAlphaKavu();
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
