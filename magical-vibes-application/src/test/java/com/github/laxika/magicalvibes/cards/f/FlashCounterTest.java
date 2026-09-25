package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.d.DaringApprentice;
import com.github.laxika.magicalvibes.cards.i.Inspiration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashCounter.class, Inspiration.class, Concentrate.class, DaringApprentice.class})
class FlashCounterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting an instant spell")
    void castingTargetsInstantSpell() {
        Inspiration inspiration = new Inspiration();
        harness.setHand(player1, List.of(inspiration));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, inspiration.getId());

        assertThat(gd.stack).hasSize(2);
        StackEntry flashCounterEntry = gd.stack.getLast();
        assertThat(flashCounterEntry.getTargetId()).isEqualTo(inspiration.getId());
    }

    @Test
    @DisplayName("Resolving counters the instant spell")
    void countersInstantSpell() {
        Inspiration inspiration = new Inspiration();
        harness.setHand(player1, List.of(inspiration));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, inspiration.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inspiration");
        assertThat(gd.stack).isEmpty();
        // Countered spell never resolved, so its draw effect did not happen.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-instant spell")
    void cannotTargetNonInstantSpell() {
        Concentrate concentrate = new Concentrate();
        harness.castFromHand(player1, concentrate, "{2}{U}{U}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, concentrate.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        DaringApprentice apprentice = new DaringApprentice();
        addCreatureReady(player1, apprentice);

        Concentrate concentrate = new Concentrate();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, concentrate, "{2}{U}{U}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, concentrate.getId());

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, apprentice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
