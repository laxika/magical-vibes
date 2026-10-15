package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarnSilverGolem;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.r.RampagingHippo;
import com.github.laxika.magicalvibes.cards.w.WithstandDeath;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HourOfDevastation.class, ChandraNalaar.class, GrizzlyBears.class,
        NicolBolasPlaneswalker.class, WithstandDeath.class, RampagingHippo.class,
        DarksteelColossus.class, KarnSilverGolem.class, LiquimetalCoating.class})
class HourOfDevastationTest extends BaseCardTest {

    private void castAndResolve() {
        harness.setHand(player1, List.of(new HourOfDevastation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Deals 5 damage to each creature, killing 2/2s on both sides")
    void dealsFiveToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Strips indestructible so lethal damage destroys the creature")
    void stripsIndestructibleThenDamages() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a non-Bolas planeswalker")
    void damagesNonBolasPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        castAndResolve();

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    @DisplayName("Does not damage a Bolas planeswalker")
    void sparesBolasPlaneswalker() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        bolas.setCounterCount(CounterType.LOYALTY, 5);

        castAndResolve();

        harness.assertOnBattlefield(player2, "Nicol Bolas, Planeswalker");
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castAndResolve();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Surviving creatures on both sides take exactly five damage and lose indestructible")
    void survivingCreaturesLoseIndestructible() {
        Permanent ownHippo = harness.addToBattlefieldAndReturn(player1, new RampagingHippo());
        Permanent opposingHippo = harness.addToBattlefieldAndReturn(player2, new RampagingHippo());
        for (Permanent hippo : List.of(ownHippo, opposingHippo)) {
            harness.setHand(player1, List.of(new WithstandDeath()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0, hippo.getId());
            assertThat(gqs.hasKeyword(gd, hippo, Keyword.INDESTRUCTIBLE)).isTrue();
        }

        castAndResolve();

        harness.assertOnBattlefield(player1, "Rampaging Hippo");
        harness.assertOnBattlefield(player2, "Rampaging Hippo");
        for (Permanent hippo : List.of(ownHippo, opposingHippo)) {
            assertThat(hippo.getMarkedDamage()).isEqualTo(5);
            assertThat(gqs.hasKeyword(gd, hippo, Keyword.INDESTRUCTIBLE)).isFalse();
        }

        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, ownHippo.getId());
        assertThat(gqs.hasKeyword(gd, ownHippo, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Printed indestructible returns after cleanup, and later entrants are unaffected")
    void removalExpiresAndDoesNotAffectLaterEntrants() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new DarksteelColossus());

        castAndResolve();

        assertThat(original.getMarkedDamage()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isFalse();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player2, new DarksteelColossus());
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(newcomer.getMarkedDamage()).isZero();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(original.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Surviving non-Bolas planeswalkers on both sides lose exactly five loyalty")
    void survivingPlaneswalkersLoseFiveLoyalty() {
        Permanent ownChandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        Permanent opposingChandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        ownChandra.setCounterCount(CounterType.LOYALTY, 6);
        opposingChandra.setCounterCount(CounterType.LOYALTY, 6);

        castAndResolve();

        harness.assertOnBattlefield(player1, "Chandra Nalaar");
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
        assertThat(ownChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(opposingChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Bolas planeswalker that is also a creature still takes five damage")
    void damagesBolasThatIsAlsoCreature() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasPlaneswalker());
        bolas.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.addToBattlefield(player1, new KarnSilverGolem());

        harness.activateAbility(player1, 1, null, bolas.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, bolas.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, bolas)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, bolas)).isTrue();

        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Nicol Bolas, Planeswalker");
        harness.assertInGraveyard(player1, "Nicol Bolas, Planeswalker");
    }
}
