package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaGeyser.class, Mountain.class, FurnaceWhelp.class})
class ManaGeyserTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one red mana for each tapped land controlled by an opponent")
    void addsRedManaForEachTappedOpponentLand() {
        Permanent ownTappedLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        ownTappedLand.tap();
        Permanent tappedOpponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        tappedOpponentLand.tap();
        Permanent secondTappedOpponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        secondTappedOpponentLand.tap();
        harness.addToBattlefield(player2, new Mountain());
        Permanent tappedOpponentCreature = harness.addToBattlefieldAndReturn(player2, new FurnaceWhelp());
        tappedOpponentCreature.tap();

        harness.castFromHand(player1, new ManaGeyser(), "{3}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts tapped lands when the spell resolves")
    void countsTappedLandsAtResolution() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.castFromHand(player1, new ManaGeyser(), "{3}{R}{R}");
        opponentLand.tap();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds no mana when no opponent land is tapped")
    void addsNoManaWhenNoOpponentLandIsTapped() {
        Permanent ownTappedLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        ownTappedLand.tap();
        harness.addToBattlefield(player2, new Mountain());

        harness.castFromHand(player1, new ManaGeyser(), "{3}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
