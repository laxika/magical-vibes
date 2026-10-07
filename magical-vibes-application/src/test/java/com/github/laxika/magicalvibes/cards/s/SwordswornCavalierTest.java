package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpeditionLookout;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordswornCavalier.class, ExpeditionLookout.class})
class SwordswornCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("Gains first strike after another Knight enters under its controller's control")
    void gainsFirstStrikeAfterAnotherKnightEnters() {
        Permanent cavalier = addCreatureReady(player1, new SwordswornCavalier());
        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();

        harness.castFromHand(player1, new ExpeditionLookout(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();

        harness.castFromHand(player1, new SwordswornCavalier(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses the conditional first strike at the end of the turn")
    void losesFirstStrikeAtEndOfTurn() {
        Permanent cavalier = addCreatureReady(player1, new SwordswornCavalier());

        harness.castFromHand(player1, new SwordswornCavalier(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Its own entry does not grant first strike")
    void ownEntryDoesNotGrantFirstStrike() {
        harness.castFromHand(player1, new SwordswornCavalier(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Swordsworn Cavalier"),
                Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Knight entering does not grant first strike")
    void opponentsKnightDoesNotGrantFirstStrike() {
        Permanent cavalier = addCreatureReady(player1, new SwordswornCavalier());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SwordswornCavalier(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A Knight that entered before the Cavalier qualifies for both Cavaliers")
    void earlierKnightEntryGrantsFirstStrike() {
        harness.castFromHand(player1, new SwordswornCavalier(), "{1}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new SwordswornCavalier(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Swordsworn Cavalier")).hasSize(2)
                .allSatisfy(cavalier -> assertThat(gqs.hasKeyword(gd, cavalier,
                        Keyword.FIRST_STRIKE)).isTrue());
    }

    @Test
    @DisplayName("First strike persists after the qualifying Knight leaves the battlefield")
    void firstStrikePersistsAfterKnightLeaves() {
        Permanent cavalier = addCreatureReady(player1, new SwordswornCavalier());
        harness.castFromHand(player1, new SwordswornCavalier(), "{1}{W}");
        harness.passBothPriorities();
        Permanent otherKnight = findPermanents(player1, "Swordsworn Cavalier").stream()
                .filter(permanent -> permanent != cavalier).findFirst().orElseThrow();

        assertThat(harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, otherKnight)).isTrue();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();
    }
}
