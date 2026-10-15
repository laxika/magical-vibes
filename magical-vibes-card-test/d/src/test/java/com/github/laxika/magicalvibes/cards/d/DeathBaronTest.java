package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShieldsOfVelisVel;
import com.github.laxika.magicalvibes.cards.s.SkeletalKathari;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathBaron.class, Gravecrawler.class, GrizzlyBears.class,
        SkeletalKathari.class, ShieldsOfVelisVel.class})
class DeathBaronTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombies you control get +1/+1 and deathtouch")
    void buffsOtherZombies() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new DeathBaron());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Death Baron does not buff itself")
    void doesNotBuffItself() {
        Permanent baron = harness.addToBattlefieldAndReturn(player1, new DeathBaron());

        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, baron, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not buff Zombies controlled by an opponent")
    void doesNotBuffOpponentZombies() {
        harness.addToBattlefield(player1, new DeathBaron());
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentZombie, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Zombie, non-Skeleton creatures")
    void doesNotBuffOtherCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DeathBaron());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Two Death Barons buff each other and stack on other Zombies")
    void twoBaronsStack() {
        harness.addToBattlefield(player1, new DeathBaron());
        harness.addToBattlefield(player1, new DeathBaron());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());

        for (Permanent baron : findPermanents(player1, "Death Baron")) {
            assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, baron, Keyword.DEATHTOUCH)).isTrue();
        }

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Bonus is removed when Death Baron leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new DeathBaron());
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Death Baron"));

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Skeletons you control get +1/+1 and deathtouch")
    void buffsSkeletons() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new SkeletalKathari());
        harness.addToBattlefield(player1, new DeathBaron());

        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, skeleton, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Skeletons do not receive the bonus")
    void doesNotBuffOpponentSkeletons() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player2, new SkeletalKathari());
        harness.addToBattlefield(player1, new DeathBaron());

        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, skeleton, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Death Baron receives its own bonus when it becomes a Skeleton")
    void buffsItselfWhenItBecomesSkeleton() {
        Permanent baron = harness.addToBattlefieldAndReturn(player1, new DeathBaron());
        harness.setHand(player1, List.of(new ShieldsOfVelisVel()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, baron, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A creature that is both a Skeleton and a Zombie receives the bonus once")
    void skeletonZombieReceivesBonusOnlyOnce() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new SkeletalKathari());
        harness.addToBattlefield(player1, new DeathBaron());
        harness.setHand(player1, List.of(new ShieldsOfVelisVel()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, skeleton, Keyword.DEATHTOUCH)).isTrue();
    }
}
