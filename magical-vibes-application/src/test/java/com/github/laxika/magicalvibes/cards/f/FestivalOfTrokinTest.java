package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({FestivalOfTrokin.class, AlabornTrooper.class, Forest.class})
class FestivalOfTrokinTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each creature you control")
    void gains2LifePerCreature() {
        harness.addToBattlefield(player1, new AlabornTrooper());
        harness.addToBattlefield(player1, new AlabornTrooper());
        harness.addToBattlefield(player1, new AlabornTrooper());
        harness.setHand(player1, List.of(new FestivalOfTrokin()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        // 3 creatures × 2 life = 6 life gained
        harness.assertLife(player1, lifeBefore + 6);
    }

    @Test
    @DisplayName("Only counts creatures you control")
    void onlyCountsControlledCreatures() {
        harness.addToBattlefield(player1, new AlabornTrooper());
        harness.addToBattlefield(player2, new AlabornTrooper());
        harness.addToBattlefield(player2, new AlabornTrooper());
        harness.setHand(player1, List.of(new FestivalOfTrokin()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        // Only 1 controlled creature × 2 life = 2 life gained
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Gains no life with no creatures")
    void gainsNoLifeWithNoCreatures() {
        harness.setHand(player1, List.of(new FestivalOfTrokin()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void doesNotCountNoncreaturePermanents() {
        harness.addToBattlefield(player1, new AlabornTrooper());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new FestivalOfTrokin()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Counts creatures when the spell resolves")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new AlabornTrooper());
        harness.setHand(player1, List.of(new FestivalOfTrokin()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new AlabornTrooper());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
    }
}
