package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kaleidoscorch.class})
class KaleidoscorchTest extends BaseCardTest {

    @Test
    @DisplayName("Converge damage counts distinct colors spent")
    void convergeDamageCountsDistinctColors() {
        harness.setHand(player1, List.of(new Kaleidoscorch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Flashback deals converge damage and exiles the spell after resolving")
    void flashbackDealsDamageAndExilesSpell() {
        Card spell = new Kaleidoscorch();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }
}
