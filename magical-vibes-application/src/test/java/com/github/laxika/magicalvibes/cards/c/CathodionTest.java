package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cathodion.class, WrathOfGod.class})
class CathodionTest extends BaseCardTest {

    @Test
    @DisplayName("When Cathodion dies, its controller adds three colorless mana")
    void diesAddsThreeColorlessMana() {
        harness.addToBattlefield(player1, new Cathodion());

        GameData gd = harness.getGameData();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cathodion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }
}
