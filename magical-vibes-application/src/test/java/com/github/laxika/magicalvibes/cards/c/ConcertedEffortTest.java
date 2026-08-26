package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WeatherseedFaeries;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConcertedEffort.class, GrizzlyBears.class, CloudSprite.class, WeatherseedFaeries.class})
class ConcertedEffortTest extends BaseCardTest {

    private void resolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Shares keywords present among creatures you control at upkeep")
    void sharesKeywords() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new CloudSprite());
        bears.getGrantedKeywords().add(Keyword.ISLANDWALK);
        bears.getGrantedKeywords().add(Keyword.TRAMPLE);

        resolveUpkeepTrigger(player1);

        assertThat(gqs.hasKeyword(gd, sprite, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Shares protection abilities from creatures you control")
    void sharesProtection() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent faeries = harness.addToBattlefieldAndReturn(player1, new WeatherseedFaeries());

        resolveUpkeepTrigger(player1);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, faeries, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Shared abilities wear off at end of turn")
    void sharedAbilitiesWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CloudSprite());

        resolveUpkeepTrigger(player1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}
