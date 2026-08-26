package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaquatussDisdain.class, ThinkTwice.class})
class LaquatussDisdainTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell cast from a graveyard and draws a card")
    void countersGraveyardCastAndDrawsCard() {
        ThinkTwice spell = new ThinkTwice();
        harness.setGraveyard(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 3);
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
        ThinkTwice spell = new ThinkTwice();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0);

        harness.setHand(player1, List.of(new LaquatussDisdain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell cast from a graveyard");
    }
}
