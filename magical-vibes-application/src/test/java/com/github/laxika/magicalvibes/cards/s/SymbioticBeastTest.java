package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SymbioticBeast.class, WrathOfGod.class})
class SymbioticBeastTest extends BaseCardTest {

    @Test
    @DisplayName("When Symbiotic Beast dies, its controller creates four Insect tokens")
    void deathTriggerCreatesFourInsectTokens() {
        harness.addToBattlefield(player1, new SymbioticBeast());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Symbiotic Beast");
        assertThat(countPermanents(player1, "Insect")).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Insect");
    }
}
