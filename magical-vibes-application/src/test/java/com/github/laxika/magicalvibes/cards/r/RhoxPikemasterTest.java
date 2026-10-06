package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxPikemaster.class, EliteVanguard.class, RuneclawBear.class})
class RhoxPikemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Own Soldier creature gains first strike")
    void ownSoldierGainsFirstStrike() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Rhox Pikemaster kills its blocker before taking combat damage")
    void innateFirstStrikeDealsDamageBeforeBlocker() {
        Permanent pikemaster = addCreatureReady(player1, new RhoxPikemaster());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Rhox Pikemaster");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(pikemaster.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not grant first strike to non-Soldier creature")
    void doesNotGrantToNonSoldier() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike to opponent's Soldier creature")
    void doesNotGrantToOpponentSoldier() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike is lost when Rhox Pikemaster leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isTrue();

        // Remove the lord
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Rhox Pikemaster"));

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Two Rhox Pikemasters grant first strike to each other")
    void twoPikemastersGrantToEachOther() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        harness.addToBattlefield(player1, new RhoxPikemaster());

        List<Permanent> pikemasters = findPermanents(player1, "Rhox Pikemaster");

        assertThat(pikemasters).hasSize(2);
        for (Permanent pikemaster : pikemasters) {
            // Each has innate first strike + receives it from the other (redundant but correct)
            assertThat(gqs.hasKeyword(gd, pikemaster, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("A Soldier kills its blocker before the blocker can deal damage")
    void grantedFirstStrikeWinsCombat() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(vanguard.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A Soldier already on the battlefield gains first strike when Pikemaster enters")
    void existingSoldierGainsFirstStrikeWhenLordEnters() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new RhoxPikemaster());

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing one of two Pikemasters preserves first strike until the last leaves")
    void firstStrikeRemainsWhileAnotherLordIsPresent() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RhoxPikemaster());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RhoxPikemaster());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        first.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isTrue();

        second.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isFalse();
    }

}
