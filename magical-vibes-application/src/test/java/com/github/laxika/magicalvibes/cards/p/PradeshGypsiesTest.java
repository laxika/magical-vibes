package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PradeshGypsiesTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -2/-0 until end of turn when the ability resolves")
    void weakensTargetCreature() {
        setupGypsies();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps the Gypsies when activated")
    void tapsOnActivation() {
        setupGypsies();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Pradesh Gypsies").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Weakening wears off at cleanup")
    void weakeningWearsOff() {
        setupGypsies();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    private void setupGypsies() {
        harness.addToBattlefield(player1, new PradeshGypsies());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Pradesh Gypsies").setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
