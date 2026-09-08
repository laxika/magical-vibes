package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InventiveWingsmith.class, GrizzlyBears.class, PrecognitionField.class, Shock.class})
class InventiveWingsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a flying counter on itself at the end step when no spell was cast from hand")
    void putsFlyingCounterWhenNoSpellWasCastFromHand() {
        Permanent wingsmith = addWingsmith();

        advanceToEndStep(player1);

        assertThat(wingsmith.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a flying counter on itself after a hand spell was cast")
    void doesNotPutCounterAfterHandSpell() {
        Permanent wingsmith = addWingsmith();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(wingsmith.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    @DisplayName("Counts a spell cast from the library separately from a hand spell")
    void countsOnlyHandSpells() {
        Permanent wingsmith = addWingsmith();
        harness.addToBattlefield(player1, new PrecognitionField());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromLibraryTop(player1, player2.getId());
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(wingsmith.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    private Permanent addWingsmith() {
        return harness.addToBattlefieldAndReturn(player1, new InventiveWingsmith());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
