package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrderedMigration.class, Plains.class, Island.class, Swamp.class,
        Mountain.class, Forest.class, CoastalTower.class})
class OrderedMigrationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one flying Bird token for each basic land type")
    void createsBirdsForEachBasicLandType() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.castFromHand(player1, new OrderedMigration(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(6)
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.BLUE);
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.BIRD);
                    assertThat(permanent.hasKeyword(Keyword.FLYING)).isTrue();
                    assertThat(permanent.getEffectivePower()).isEqualTo(1);
                    assertThat(permanent.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Counts duplicate basic land types only once")
    void countsDuplicateBasicLandTypesOnce() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new OrderedMigration(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Counts basic land types only among lands controlled by the spell's controller")
    void countsOnlyControllerLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        harness.castFromHand(player1, new OrderedMigration(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Creates no Bird tokens when the controller has no basic land types")
    void createsNoBirdsWithoutBasicLandTypes() {
        harness.castFromHand(player1, new OrderedMigration(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Creates five Birds when all five basic land types are present")
    void createsFiveBirdsForAllBasicLandTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new OrderedMigration(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(5);
    }

    @Test
    @DisplayName("A land producing white and blue mana without basic land types contributes no Birds")
    void ignoresManaColorsOfUntypedLands() {
        harness.addToBattlefield(player1, new CoastalTower());

        harness.castFromHand(player1, new OrderedMigration(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Counts basic land types at resolution rather than when cast")
    void countsLandTypesAtResolution() {
        harness.addToBattlefield(player1, new Plains());
        harness.castFromHand(player1, new OrderedMigration(), "{3}{W}{U}");

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }
}
