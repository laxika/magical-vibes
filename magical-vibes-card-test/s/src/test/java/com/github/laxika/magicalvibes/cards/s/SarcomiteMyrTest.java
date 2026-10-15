package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void flyingAbilityWorksWhileTappedAndOnlyAffectsItsSource() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new SarcomiteMyr());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SarcomiteMyr());
        myr.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, myr, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myr, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    void sacrificeIsPaidImmediatelyButDrawWaitsForResolution() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new SarcomiteMyr());
        myr.tap();
        SarcomiteMyr drawnCard = new SarcomiteMyr();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Sarcomite Myr");
        harness.assertInGraveyard(player1, "Sarcomite Myr");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
