package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AraAHeartOfTheSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicSpiderMan.class, AraAHeartOfTheSpider.class, GrizzlyBears.class})
class CosmicSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Other Spiders you control gain all five keywords at the beginning of combat")
    void grantsKeywordsToOtherSpiders() {
        harness.addToBattlefield(player1, new CosmicSpiderMan());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AraAHeartOfTheSpider());
        Permanent nonSpider = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingSpider = harness.addToBattlefieldAndReturn(player2, new AraAHeartOfTheSpider());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSpider, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSpider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new CosmicSpiderMan());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AraAHeartOfTheSpider());

        advanceToCombatAndResolve(player2);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.HASTE)).isFalse();
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
