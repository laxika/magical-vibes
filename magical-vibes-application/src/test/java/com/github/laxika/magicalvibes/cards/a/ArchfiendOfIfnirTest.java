package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchfiendOfIfnirTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card puts a -1/-1 counter on each creature opponents control")
    void cyclingCountersOpponentCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfIfnir());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        // A second cycling card to cycle — cycling is a discard, so it triggers the ability.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the -1/-1 trigger

        assertThat(oppBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ownBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cycling Archfiend of Ifnir itself does not trigger the ability")
    void cyclingSelfDoesNotTrigger() {
        // The ability only functions on the battlefield; cycling Archfiend from hand can't trigger it.
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArchfiendOfIfnir()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(oppBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        harness.assertInGraveyard(player1, "Archfiend of Ifnir");
    }
}
