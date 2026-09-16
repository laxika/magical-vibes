package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BogDown;
import com.github.laxika.magicalvibes.cards.g.GaeasHerald;
import com.github.laxika.magicalvibes.cards.l.LashknifeBarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderscapeFamiliar.class, BogDown.class, GaeasHerald.class, LashknifeBarrier.class})
class ThunderscapeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Black spells you cast cost {1} less")
    void blackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new ThunderscapeFamiliar());
        harness.setHand(player1, List.of(new BogDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Bog Down"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Green spells you cast cost {1} less")
    void greenSpellsCostOneLess() {
        harness.addToBattlefield(player1, new ThunderscapeFamiliar());
        harness.setHand(player1, List.of(new GaeasHerald()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Gaea's Herald"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spells of other colors are not reduced")
    void otherColorsAreNotReduced() {
        harness.addToBattlefield(player1, new ThunderscapeFamiliar());
        harness.setHand(player1, List.of(new LashknifeBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new ThunderscapeFamiliar());
        harness.setHand(player2, List.of(new BogDown()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
