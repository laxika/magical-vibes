package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TromellSeymoursButler.class, DragonFodder.class, GrizzlyBears.class})
class TromellSeymoursButlerTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creatures you control enter with an additional +1/+1 counter")
    void nontokenCreatureEntersWithAdditionalCounter() {
        harness.addToBattlefield(player1, new TromellSeymoursButler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability proliferates once per nontoken creature entered this turn")
    void proliferatesForNontokenCreaturesEnteredThisTurn() {
        Permanent tromell = addCreatureReady(player1, new TromellSeymoursButler());

        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(tromell.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
