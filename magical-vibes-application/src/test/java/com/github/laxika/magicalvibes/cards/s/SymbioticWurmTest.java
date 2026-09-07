package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SymbioticWurm.class, WrathOfGod.class})
class SymbioticWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Symbiotic Wurm dies, its controller creates seven Insect tokens")
    void deathTriggerCreatesSevenInsectTokens() {
        harness.addToBattlefield(player1, new SymbioticWurm());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Symbiotic Wurm");
        assertThat(countPermanents(player1, "Insect")).isEqualTo(7);
        harness.assertNotOnBattlefield(player2, "Insect");
    }
}
