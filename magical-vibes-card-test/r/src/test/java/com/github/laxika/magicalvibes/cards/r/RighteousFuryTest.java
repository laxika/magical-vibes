package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RighteousFury.class, AlabornTrooper.class, Plains.class, DrudgeSkeletons.class})
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
        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertInGraveyard(player1, "Alaborn Trooper");
        harness.assertInGraveyard(player2, "Alaborn Trooper");
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

    @Test
    @DisplayName("Regenerated creatures survive and do not contribute to life gained")
    void regeneratedCreatureDoesNotCountAsDestroyed() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        skeletons.tap();
        Permanent trooper = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        trooper.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new RighteousFury(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        harness.assertNotInGraveyard(player1, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Alaborn Trooper");
        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertLife(player1, STARTING_LIFE + 2);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Uses creatures' tapped state at resolution")
    void checksTappedStateAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.castFromHand(player1, new RighteousFury(), "{4}{W}{W}");
        creature.tap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertInGraveyard(player2, "Alaborn Trooper");
        harness.assertLife(player1, STARTING_LIFE + 2);
        harness.assertLife(player2, STARTING_LIFE);
    }
}
