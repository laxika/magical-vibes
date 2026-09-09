package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatedSliver;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardSliver.class, PlatedSliver.class, GrizzlyBears.class})
class WardSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen color protection applies to all Slivers")
    void grantsProtectionFromChosenColorToAllSlivers() {
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new PlatedSliver());
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new PlatedSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new WardSliver()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent wardSliver = findPermanent(player1, "Ward Sliver");
        assertThat(gqs.hasProtectionFrom(gd, wardSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nonSliver, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownSliver, CardColor.BLUE)).isFalse();
    }
}
