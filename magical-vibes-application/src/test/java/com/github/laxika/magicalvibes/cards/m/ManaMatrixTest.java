package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SylvanLibrary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaMatrix.class, AngelsMercy.class, SylvanLibrary.class, Divination.class, GrizzlyBears.class})
class ManaMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Instant spells you cast cost {2} less")
    void instantSpellsAreReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Enchantment spells you cast cost {2} less")
    void enchantmentSpellsAreReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new SylvanLibrary()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sorcery and creature spells are not reduced")
    void otherSpellTypesAreNotReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        gd.playerManaPools.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to opponents' spells")
    void opponentsSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player2, List.of(new AngelsMercy()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
