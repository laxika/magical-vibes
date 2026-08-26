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
        harness.setHand(player1, List.of(new BriarbridgeTracker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new BriarbridgeTracker()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
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
}
