package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwarvenReinforcementsTest extends BaseCardTest {

    @Test
    void createsTwoDwarfBerserkers() {
        harness.setHand(player1, List.of(new DwarvenReinforcements()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Dwarf Berserker");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.DWARF, CardSubtype.BERSERKER);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void canBeCastFromExileForItsForetellCost() {
        DwarvenReinforcements card = new DwarvenReinforcements();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, card.getId(), 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dwarf Berserker")).hasSize(2);
    }
}
