package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrindleShoat.class, WrathOfGod.class})
class BrindleShoatTest extends BaseCardTest {

    @Test
    @DisplayName("When Brindle Shoat dies, its controller creates a 3/3 Boar token")
    void deathTriggerCreatesBoarToken() {
        harness.addToBattlefield(player1, new BrindleShoat());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brindle Shoat");
        assertThat(countPermanents(player1, "Boar")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Boar");
    }
}
