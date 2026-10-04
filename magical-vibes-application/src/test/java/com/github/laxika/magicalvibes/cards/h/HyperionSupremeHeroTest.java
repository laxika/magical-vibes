package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaUnbowed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HyperionSupremeHero.class, GrizzlyBears.class, LightningBolt.class, Shock.class,
        CaptainAmericaUnbowed.class, Humble.class})
class HyperionSupremeHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all but one damage to its controller")
    void protectsController() {
        addCreatureReady(player1, new HyperionSupremeHero());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents all but one damage to a Hero it controls")
    void protectsHero() {
        Permanent hyperion = addCreatureReady(player1, new HyperionSupremeHero());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hyperion.getId());

        assertThat(hyperion.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hyperion, Supreme Hero");
    }

    @Test
    @DisplayName("Does not protect a non-Hero creature")
    void doesNotProtectNonHeroCreature() {
        addCreatureReady(player1, new HyperionSupremeHero());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void protectsAnotherHero() {
        addCreatureReady(player1, new HyperionSupremeHero());
        Permanent hero = addCreatureReady(player1, new CaptainAmericaUnbowed());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hero.getId());

        assertThat(hero.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotProtectOpposingHero() {
        addCreatureReady(player1, new HyperionSupremeHero());
        Permanent hero = addCreatureReady(player2, new CaptainAmericaUnbowed());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, hero.getId());

        assertThat(hero.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void doesNotProtectOpponent() {
        addCreatureReady(player1, new HyperionSupremeHero());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void appliesSeparatelyToEachDamageEvent() {
        addCreatureReady(player1, new HyperionSupremeHero());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void reducesEachCombatSourceSeparately() {
        addCreatureReady(player1, new HyperionSupremeHero());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.resolveCombatDamage();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void losingAbilitiesStopsProtectingController() {
        Permanent hyperion = addCreatureReady(player1, new HyperionSupremeHero());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Humble(), new LightningBolt()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hyperion.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void losingAbilitiesStopsProtectingOtherHeroes() {
        Permanent hyperion = addCreatureReady(player1, new HyperionSupremeHero());
        Permanent hero = addCreatureReady(player1, new CaptainAmericaUnbowed());
        harness.setHand(player2, List.of(new Humble(), new LightningBolt()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hyperion.getId());
        harness.castAndResolveInstant(player2, 0, hero.getId());

        assertThat(hero.getMarkedDamage()).isEqualTo(3);
    }
}
