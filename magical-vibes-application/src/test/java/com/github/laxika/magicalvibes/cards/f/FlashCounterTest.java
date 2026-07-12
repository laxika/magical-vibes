package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashCounterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting an instant spell")
    void castingTargetsInstantSpell() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, mercy.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry flashCounterEntry = gd.stack.getLast();
        assertThat(flashCounterEntry.getTargetId()).isEqualTo(mercy.getId());
    }

    @Test
    @DisplayName("Resolving counters the instant spell")
    void countersInstantSpell() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setLife(player1, 13);
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, mercy.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angel's Mercy"));
        assertThat(gd.stack).isEmpty();
        // Countered spell never resolved, so no life was gained.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Cannot target a non-instant spell")
    void cannotTargetNonInstantSpell() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, divination.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
