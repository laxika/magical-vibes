package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BriarbridgeTracker.class, Forest.class})
class BriarbridgeTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates on entering and gets +2/+0 while controlling the Clue")
    void investigatesAndGetsBoosted() {
        harness.castFromHand(player1, new BriarbridgeTracker(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Briarbridge Tracker");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Has base power and toughness without a token")
    void noTokenMeansNoBoost() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new BriarbridgeTracker());

        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the boost when its Clue is sacrificed")
    void losesBoostWhenClueLeaves() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new BriarbridgeTracker(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Briarbridge Tracker");
        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);

        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A token copy counts itself for the power bonus")
    void tokenCopyCountsItself() {
        BriarbridgeTracker tokenCopy = new BriarbridgeTracker();
        tokenCopy.setToken(true);
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, tokenCopy);

        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's token does not grant the power bonus")
    void opponentsTokenDoesNotGrantBonus() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new BriarbridgeTracker());
        BriarbridgeTracker tokenCopy = new BriarbridgeTracker();
        tokenCopy.setToken(true);
        harness.addToBattlefield(player2, tokenCopy);

        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature tokens grant the bonus only once regardless of their number")
    void multipleCreatureTokensGrantOnlyOneBonus() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new BriarbridgeTracker());
        for (int i = 0; i < 2; i++) {
            BriarbridgeTracker tokenCopy = new BriarbridgeTracker();
            tokenCopy.setToken(true);
            harness.addToBattlefield(player1, tokenCopy);
        }

        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }
}
