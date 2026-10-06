package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShepherdOfHeroes.class, BoggartBrute.class, FaerieMiscreant.class, FugitiveWizard.class,
        StoneworkPackbeast.class, SeaGateColossus.class, IntoTheRoil.class})
class ShepherdOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each creature in your party, including itself")
    void gainsTwoLifePerPartyRole() {
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setLife(player1, 10);

        castShepherd();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts only creatures in your party")
    void ignoresOpponentsPartyCreatures() {
        harness.addToBattlefield(player2, new FaerieMiscreant());
        harness.addToBattlefield(player2, new BoggartBrute());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLife(player1, 10);

        castShepherd();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Duplicate party roles do not increase life gain")
    void countsEachRoleOnlyOnce() {
        harness.addToBattlefield(player1, new ShepherdOfHeroes());
        harness.addToBattlefield(player1, new SeaGateColossus());
        harness.addToBattlefield(player1, new SeaGateColossus());
        harness.setLife(player1, 10);

        castShepherd();

        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("One creature with all party types counts as only one member")
    void countsMultitypeCreatureOnlyOnce() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.setLife(player1, 10);

        castShepherd();

        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Multiple creatures with all party types maximize the party up to four")
    void maximizesPartyWithoutExceedingFour() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }
        harness.setLife(player1, 10);

        castShepherd();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Party size is evaluated when the trigger resolves, even if Shepherd leaves")
    void gainsNoLifeWhenOnlyPartyMemberLeavesBeforeResolution() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new ShepherdOfHeroes(), "{4}{W}");
        harness.passBothPriorities();
        var shepherd = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.assertLife(player1, 10);
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, shepherd.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shepherd of Heroes");
        harness.assertLife(player1, 10);
    }

    private void castShepherd() {
        harness.castFromHand(player1, new ShepherdOfHeroes(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
