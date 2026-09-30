package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.Lunge;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SevinneTheChronoclasm.class, Lunge.class, ThinkTwice.class})
class SevinneTheChronoclasmTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage dealt to Sevinne")
    void preventsDamageToSelf() {
        Permanent sevinne = addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(sevinne.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(sevinne.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Sevinne, the Chronoclasm");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Copies the first instant or sorcery cast from the graveyard each turn")
    void copiesFirstGraveyardSpellEachTurn() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        ThinkTwice first = new ThinkTwice();
        ThinkTwice second = new ThinkTwice();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castFromGraveyard(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Does not copy a second graveyard spell in the same turn")
    void doesNotCopySecondGraveyardSpellEachTurn() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castFromGraveyard(player1, 0);
        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }
}
