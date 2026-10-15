package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterbloomBearer.class})
class BitterbloomBearerTest extends BaseCardTest {

    @Test
    @DisplayName("At its controller's upkeep, loses 1 life and creates a flying blue-black Faerie")
    void losesLifeAndCreatesFaerieAtUpkeep() {
        harness.addToBattlefield(player1, new BitterbloomBearer());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Faerie");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();

        assertThat(token.getCard().getName()).isEqualTo("Faerie");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FAERIE);
        assertThat(token.getCard().getKeywords()).containsExactly(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BitterbloomBearer());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken())).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep without triggering immediately")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.castFromHand(player1, new BitterbloomBearer(), "{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bitterbloom Bearer");
        harness.assertLife(player1, 20);
        assertThat(countPermanents(player1, "Faerie")).isZero();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Bearer creates one token and loses one life")
    void multipleBearersTriggerIndependently() {
        harness.addToBattlefield(player1, new BitterbloomBearer());
        harness.addToBattlefield(player1, new BitterbloomBearer());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(2);
        assertThat(countPermanents(player2, "Faerie")).isZero();
    }

    @Test
    @DisplayName("The upkeep ability resolves even after its source leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BitterbloomBearer());
        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bitterbloom Bearer");
        harness.assertLife(player1, 19);
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
    }
}
