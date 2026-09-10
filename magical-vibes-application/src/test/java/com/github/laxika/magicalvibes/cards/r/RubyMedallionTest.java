package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RubyMedallion.class, LowlandGiant.class, FightingDrake.class})
class RubyMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("Red spells you cast cost {1} less")
    void redSpellsCostOneLess() {
        harness.addToBattlefield(player1, new RubyMedallion());
        // Lowland Giant costs {2}{R}{R} — with the {1} reduction three mana is enough
        harness.setHand(player1, List.of(new LowlandGiant()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Lowland Giant"));
    }

    @Test
    @DisplayName("Non-red spells are not reduced")
    void nonRedSpellsNotReduced() {
        harness.addToBattlefield(player1, new RubyMedallion());
        // Fighting Drake costs {2}{U}{U} — not red, so three mana is not enough
        harness.setHand(player1, List.of(new FightingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new RubyMedallion());
        harness.setHand(player2, List.of(new LowlandGiant()));
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not remove colored mana requirements")
    void reductionOnlyRemovesGenericMana() {
        harness.addToBattlefield(player1, new RubyMedallion());
        harness.setHand(player1, List.of(new LowlandGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
