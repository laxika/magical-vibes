package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TreetopFreedomFighters.class)
class TreetopFreedomFightersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Ally token")
    void etbCreatesAllyToken() {
        harness.castFromHand(player1, new TreetopFreedomFighters(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCard().isToken()).isTrue();
        assertThat(ally.getCard().getPower()).isEqualTo(1);
        assertThat(ally.getCard().getToughness()).isEqualTo(1);
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).contains(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("The Ally is created by the enter trigger, not while the creature spell resolves")
    void tokenWaitsForEnterTriggerToResolve() {
        harness.castFromHand(player1, new TreetopFreedomFighters(), "{2}{R}");
        assertThat(countPermanents(player1, "Ally")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treetop Freedom Fighters")).isEqualTo(1);
        assertThat(countPermanents(player1, "Ally")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(1);
        assertThat(countPermanents(player2, "Ally")).isZero();
    }

    @Test
    @DisplayName("Each entry without casting creates one Ally for the entering creature's controller")
    void noncastEntriesCreateTokensForController() {
        harness.enterBattlefieldAndReturn(player2, new TreetopFreedomFighters());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Ally")).isEqualTo(1);
        assertThat(countPermanents(player1, "Ally")).isZero();

        harness.enterBattlefieldAndReturn(player2, new TreetopFreedomFighters());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Ally")).isEqualTo(2);
        assertThat(countPermanents(player1, "Ally")).isZero();
        assertThat(findPermanents(player2, "Ally")).allSatisfy(ally -> {
            assertThat(ally.isTapped()).isFalse();
            assertThat(ally.isSummoningSick()).isTrue();
        });
    }
}
