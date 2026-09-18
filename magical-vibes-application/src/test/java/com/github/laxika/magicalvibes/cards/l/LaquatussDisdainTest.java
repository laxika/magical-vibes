package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaquatussDisdain.class, FlaringPain.class})
class LaquatussDisdainTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell cast from a graveyard and draws a card")
    void countersGraveyardCastAndDrawsCard() {
        FlaringPain spell = new FlaringPain();
        harness.setGraveyard(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFlashback(player2, 0);

        harness.setHand(player1, List.of(new LaquatussDisdain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a spell cast from hand")
    void cannotTargetHandCastSpell() {
        FlaringPain spell = new FlaringPain();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0);

        harness.setHand(player1, List.of(new LaquatussDisdain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell cast from a graveyard");
    }

    @Test
    @DisplayName("Does not draw when the targeted graveyard spell leaves the stack first")
    void doesNotDrawWhenTargetLeavesStackBeforeResolution() {
        FlaringPain spell = new FlaringPain();
        harness.setGraveyard(player2, List.of(spell));
        harness.setLibrary(player1, List.of(new FlaringPain()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFlashback(player2, 0);

        harness.setHand(player1, List.of(new LaquatussDisdain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, spell.getId());

        harness.setHand(player2, List.of(new LaquatussDisdain()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
