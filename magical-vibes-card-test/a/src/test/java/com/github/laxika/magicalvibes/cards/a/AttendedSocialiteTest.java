package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttendedSocialite.class, GrizzlyBears.class})
class AttendedSocialiteTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering under your control gives it +1/+1 until end of turn")
    void allyCreatureEnteringBoostsSocialite() {
        Permanent socialite = harness.addToBattlefieldAndReturn(player1, new AttendedSocialite());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(socialite.getEffectivePower()).isEqualTo(3);
        assertThat(socialite.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void opponentCreatureEnteringDoesNotTrigger() {
        Permanent socialite = harness.addToBattlefieldAndReturn(player1, new AttendedSocialite());
        harness.forceActivePlayer(player2);

        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(socialite.getEffectivePower()).isEqualTo(2);
        assertThat(socialite.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attended Socialite's own entry does not trigger it")
    void ownEntryDoesNotTrigger() {
        harness.castFromHand(player1, new AttendedSocialite(), "{1}{G}");
        harness.passBothPriorities();

        Permanent socialite = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(socialite.getEffectivePower()).isEqualTo(2);
        assertThat(socialite.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Alliance boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent socialite = harness.addToBattlefieldAndReturn(player1, new AttendedSocialite());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gqs.getEffectivePower(gameData, socialite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gameData, socialite)).isEqualTo(1);
    }

    private void castGrizzlyBears(Player player) {
        harness.castFromHand(player, new GrizzlyBears(), "{1}{G}");
    }

    @Test
    @DisplayName("Each additional creature creates a separate, cumulative boost")
    void multipleEntriesGiveCumulativeBoosts() {
        Permanent socialite = harness.addToBattlefieldAndReturn(player1, new AttendedSocialite());

        castGrizzlyBears(player1);
        resolveAllTriggers();
        castGrizzlyBears(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(socialite.getEffectivePower()).isEqualTo(3);
        assertThat(socialite.getEffectiveToughness()).isEqualTo(2);

        resolveAllTriggers();

        assertThat(socialite.getEffectivePower()).isEqualTo(4);
        assertThat(socialite.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A second Socialite boosts the existing one, not itself")
    void secondSocialiteBoostsOnlyExistingSocialite() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AttendedSocialite());

        harness.castFromHand(player1, new AttendedSocialite(), "{1}{G}");
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(first.getEffectivePower()).isEqualTo(2);

        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(1);
    }
}
