package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.p.PlatedSliver;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardSliver.class, PlatedSliver.class, FugitiveWizard.class})
class WardSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen color protection applies to all Slivers")
    void grantsProtectionFromChosenColorToAllSlivers() {
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new PlatedSliver());
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new PlatedSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        harness.castFromHand(player1, new WardSliver(), "{4}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent wardSliver = findPermanent(player1, "Ward Sliver");
        assertThat(gqs.hasProtectionFrom(gd, wardSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nonSliver, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownSliver, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Protection applies to later Slivers and ends when Ward Sliver leaves")
    void protectionTracksLaterSliversAndSourceLeaving() {
        harness.castFromHand(player1, new WardSliver(), "{4}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent laterSliver = harness.enterBattlefieldAndReturn(player2, new PlatedSliver());
        assertThat(gqs.hasProtectionFrom(gd, laterSliver, CardColor.RED)).isTrue();

        Permanent wardSliver = findPermanent(player1, "Ward Sliver");
        gd.playerBattlefields.get(player1.getId()).remove(wardSliver);

        assertThat(gqs.hasProtectionFrom(gd, laterSliver, CardColor.RED)).isFalse();
    }
}
