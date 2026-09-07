package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoathsomeChimera.class, GrizzlyBears.class})
class LoathsomeChimeraTest extends BaseCardTest {

    @Test
    void castFromHandEntersWithoutCounter() {
        harness.setHand(player1, List.of(new LoathsomeChimera()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Loathsome Chimera")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapingExilesThreeOtherCardsAndAddsCounter() {
        LoathsomeChimera chimera = new LoathsomeChimera();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chimera, first, second, third));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent escapedChimera = findPermanent(player1, "Loathsome Chimera");
        assertThat(escapedChimera.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void escapeRequiresThreeOtherCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new LoathsomeChimera(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
