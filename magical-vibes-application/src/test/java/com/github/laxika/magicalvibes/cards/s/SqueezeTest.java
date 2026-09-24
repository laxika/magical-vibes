package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.b.Bribery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Squeeze.class, Brainstorm.class, Bribery.class})
class SqueezeTest extends BaseCardTest {

    @Test
    @DisplayName("Sorcery spells cost three more to cast")
    void sorcerySpellsCostThreeMoreToCast() {
        harness.addToBattlefield(player1, new Squeeze());
        harness.setHand(player1, List.of(new Bribery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A sorcery cannot be cast without paying the additional cost")
    void sorceryCannotBeCastWithoutAdditionalCost() {
        harness.addToBattlefield(player1, new Squeeze());
        harness.setHand(player1, List.of(new Bribery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Instant spells are not taxed")
    void instantSpellsAreNotTaxed() {
        harness.addToBattlefield(player1, new Squeeze());
        harness.castFromHand(player1, new Brainstorm(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sorcery spells cast by opponents also cost three more")
    void sorcerySpellsCastByOpponentsCostThreeMore() {
        harness.addToBattlefield(player1, new Squeeze());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Bribery()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.castSorcery(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
