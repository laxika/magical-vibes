package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostflameSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class GhostflameSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Makes all Slivers colorless, including opposing Slivers")
    void makesAllSliversColorless() {
        Permanent ghostflameSliver = harness.addToBattlefieldAndReturn(player1, new GhostflameSliver());
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new BonescytheSliver());
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new BonescytheSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveColors(gd, ghostflameSliver)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, ownSliver)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, opposingSliver)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, nonSliver)).containsExactly(CardColor.GREEN);
    }
}
