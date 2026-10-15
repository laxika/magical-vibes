package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AraAHeartOfTheSpider;
import com.github.laxika.magicalvibes.cards.t.TaxiDriver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicSpiderMan.class, AraAHeartOfTheSpider.class, TaxiDriver.class})
class CosmicSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Other Spiders you control gain all five keywords at the beginning of combat")
    void grantsKeywordsToOtherSpiders() {
        harness.addToBattlefield(player1, new CosmicSpiderMan());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AraAHeartOfTheSpider());
        Permanent nonSpider = harness.addToBattlefieldAndReturn(player1, new TaxiDriver());
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
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Granted keywords last through the end step and expire during cleanup")
    void keywordsExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new CosmicSpiderMan());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AraAHeartOfTheSpider());

        advanceToCombatAndResolve(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertGrantedKeywords(spider, true);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertGrantedKeywords(spider, false);
    }

    @Test
    @DisplayName("A Spider entering after the trigger resolves does not gain keywords")
    void doesNotAffectSpidersEnteringAfterResolution() {
        harness.addToBattlefield(player1, new CosmicSpiderMan());
        advanceToCombatAndResolve(player1);

        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AraAHeartOfTheSpider());

        assertGrantedKeywords(spider, false);
    }

    @Test
    @DisplayName("A Spider entering before the trigger resolves gains all five keywords")
    void affectsSpidersPresentAtResolution() {
        harness.addToBattlefield(player1, new CosmicSpiderMan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AraAHeartOfTheSpider());
        assertGrantedKeywords(spider, false);
        resolveAllTriggers();

        assertGrantedKeywords(spider, true);
    }

    @Test
    @DisplayName("The combat trigger resolves even if Cosmic Spider-Man leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CosmicSpiderMan());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AraAHeartOfTheSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cosmic Spider-Man");
        assertGrantedKeywords(spider, true);
    }

    private void assertGrantedKeywords(Permanent permanent, boolean expected) {
        for (Keyword keyword : new Keyword[]{Keyword.FLYING, Keyword.FIRST_STRIKE,
                Keyword.TRAMPLE, Keyword.LIFELINK, Keyword.HASTE}) {
            assertThat(gqs.hasKeyword(gd, permanent, keyword)).as("%s", keyword).isEqualTo(expected);
        }
    }
}
