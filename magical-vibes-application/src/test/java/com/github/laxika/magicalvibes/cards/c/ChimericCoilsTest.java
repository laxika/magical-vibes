package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ChimericCoils.class)
class ChimericCoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Activation makes Chimeric Coils an X/X Construct artifact creature until end of turn")
    void activationAnimatesWithChosenX() {
        Permanent coils = addCreatureReady(player1, new ChimericCoils());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThat(gqs.isCreature(gd, coils)).isFalse();

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, coils)).isTrue();
        assertThat(gqs.getEffectivePower(gd, coils)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, coils)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, coils, CardSubtype.CONSTRUCT)).isTrue();
    }

    @Test
    @DisplayName("Activation sacrifices Chimeric Coils at the next end step")
    void activationSchedulesSacrificeAtNextEndStep() {
        addCreatureReady(player1, new ChimericCoils());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chimeric Coils");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chimeric Coils");
    }

    @Test
    @DisplayName("Choosing zero for X puts the resulting 0/0 artifact creature into the graveyard")
    void zeroXDiesAsZeroToughnessCreature() {
        addCreatureReady(player1, new ChimericCoils());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chimeric Coils");
        harness.assertInGraveyard(player1, "Chimeric Coils");
    }
}
