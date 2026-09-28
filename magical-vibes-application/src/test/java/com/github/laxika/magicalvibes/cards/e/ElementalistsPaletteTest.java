package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalistsPalette.class, HangarbackWalker.class, GrizzlyBears.class})
class ElementalistsPaletteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with X puts two charge counters on the Palette")
    void castingXSpellAddsTwoChargeCounters() {
        Permanent palette = addReadyPalette();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(palette.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a spell without X does not put charge counters on the Palette")
    void castingNonXSpellDoesNotAddChargeCounters() {
        Permanent palette = addReadyPalette();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(palette.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The first ability adds one mana of the chosen color")
    void addsManaOfAnyColor() {
        addReadyPalette();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds one X-cost-only mana per charge counter")
    void addsXCostOnlyManaForChargeCounters() {
        Permanent palette = addReadyPalette();
        palette.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyPalette() {
        Permanent palette = harness.addToBattlefieldAndReturn(player1, new ElementalistsPalette());
        palette.setSummoningSick(false);
        return palette;
    }
}
