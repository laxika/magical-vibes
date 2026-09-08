package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HydroManFluidFelon.class, Opt.class, Shock.class})
class HydroManFluidFelonTest extends BaseCardTest {

    @Test
    void blueSpellPumpsHydroManAndOtherColorsDoNot() {
        Permanent hydroMan = addHydroMan();
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hydroMan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydroMan)).isEqualTo(3);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hydroMan)).isEqualTo(3);
    }

    @Test
    void endStepUntapsHydroManMakesItOnlyALandAndGrantsManaUntilNextTurn() {
        Permanent hydroMan = addHydroMan();
        hydroMan.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydroMan.isTapped()).isFalse();
        assertThat(gqs.isLand(gd, hydroMan)).isTrue();
        assertThat(gqs.isCreature(gd, hydroMan)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gqs.isLand(gd, hydroMan)).isFalse();
        assertThat(gqs.isCreature(gd, hydroMan)).isTrue();
    }

    private Permanent addHydroMan() {
        return addCreatureReady(player1, new HydroManFluidFelon());
    }
}
