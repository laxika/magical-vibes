package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaSuperSoldier;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroInTraining.class, CaptainAmericaSuperSoldier.class, Forest.class})
class HeroInTrainingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card without another Hero")
    void drawsWithoutAnotherHero() {
        harness.setLibrary(player1, List.of(new Forest()));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new HeroInTraining(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 2 life after drawing when you control another Hero")
    void drawsAndGainsLifeWithAnotherHero() {
        harness.addToBattlefield(player1, new CaptainAmericaSuperSoldier());
        harness.setLibrary(player1, List.of(new Forest()));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new HeroInTraining(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void opposingHeroDoesNotEnableLifeGain() {
        harness.addToBattlefield(player2, new HeroInTraining());
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new HeroInTraining(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void heroArrivingBeforeTriggerResolvesEnablesLifeGain() {
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new HeroInTraining(), "{2}{W}");
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new HeroInTraining());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    void heroLeavingBeforeTriggerResolvesPreventsLifeGain() {
        var otherHero = harness.addToBattlefieldAndReturn(player1, new HeroInTraining());
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new HeroInTraining(), "{2}{W}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(otherHero);
        gd.playerGraveyards.get(player1.getId()).add(otherHero.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, lifeBefore);
    }
}
