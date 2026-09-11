package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RighteousFury.class, AlabornTrooper.class, Plains.class})
class RighteousFuryTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys only tapped creatures and gains 2 life per destroyed")
    void destroysTappedCreaturesAndGainsLife() {
        Permanent tapped1 = harness.addToBattlefieldAndReturn(player1, new AlabornTrooper());
        Permanent tapped2 = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new AlabornTrooper());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        tapped1.tap();
        tapped2.tap();
        tappedLand.tap();

        harness.castFromHand(player1, new RighteousFury(), "{4}{W}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(tapped1.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(tapped2.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(untapped.getId()));
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertLife(player1, STARTING_LIFE + 4);
    }

    @Test
    @DisplayName("Gains no life when no tapped creatures are on the battlefield")
    void gainsNoLifeWhenNoTappedCreatures() {
        harness.addToBattlefield(player1, new AlabornTrooper());

        harness.castFromHand(player1, new RighteousFury(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, STARTING_LIFE);
        harness.assertOnBattlefield(player1, "Alaborn Trooper");
    }
}
