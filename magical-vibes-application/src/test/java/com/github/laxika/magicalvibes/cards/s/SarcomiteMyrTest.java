package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SarcomiteMyr.class)
class SarcomiteMyrTest extends BaseCardTest {

    @Test
    void gainsFlyingUntilEndOfTurn() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new SarcomiteMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myr, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myr, Keyword.FLYING)).isFalse();
    }

    @Test
    void sacrificesItselfToDrawACard() {
        harness.addToBattlefield(player1, new SarcomiteMyr());
        GameData gameData = harness.getGameData();
        int handBefore = gameData.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sarcomite Myr");
        harness.assertInGraveyard(player1, "Sarcomite Myr");
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
