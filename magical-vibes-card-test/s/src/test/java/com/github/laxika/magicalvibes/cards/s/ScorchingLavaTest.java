package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScorchingLava.class, KavuTitan.class, MetathranZombie.class, ShamanEnKor.class})
class ScorchingLavaTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void withoutKickerLethalDamagePutsCreatureInGraveyard() {
        harness.addToBattlefield(player2, new KavuTitan());
        castOnCreature(false);

        harness.assertNotOnBattlefield(player2, "Kavu Titan");
        harness.assertInGraveyard(player2, "Kavu Titan");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Kavu Titan"));
    }

    @Test
    void kickedLethalDamageExilesCreatureInsteadOfPuttingItInGraveyard() {
        harness.addToBattlefield(player2, new KavuTitan());
        castOnCreature(true);

        harness.assertNotOnBattlefield(player2, "Kavu Titan");
        harness.assertNotInGraveyard(player2, "Kavu Titan");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Kavu Titan"));
    }

    @Test
    void kickedDamagePreventsRegeneration() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new MetathranZombie());
        skeletons.setRegenerationShield(1);
        castOnPermanent(true, skeletons);

        harness.assertNotOnBattlefield(player2, "Metathran Zombie");
        harness.assertNotInGraveyard(player2, "Metathran Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Metathran Zombie"));
    }

    @Test
    void kickedDamagePreventsRegenerationEvenWhenDamageIsPrevented() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new MetathranZombie());
        zombie.setRegenerationShield(1);
        zombie.setDamagePreventionShield(2);
        castOnPermanent(true, zombie);

        Permanent attacker = addCreatureReady(player1, new KavuTitan());
        attacker.setAttacking(true);
        zombie.setBlocking(true);
        zombie.addBlockingTargetId(attacker.getId());
        resolveCombat(player1);

        harness.assertNotOnBattlefield(player2, "Metathran Zombie");
        harness.assertNotInGraveyard(player2, "Metathran Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Metathran Zombie"));
    }

    @Test
    void kickedSpellStillDealsTwoDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void unkickedDamageAllowsRegeneration() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new MetathranZombie());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, indexOf(player2, zombie), null, null);
        harness.passBothPriorities();

        castOnPermanent(false, zombie);

        harness.assertOnBattlefield(player2, "Metathran Zombie");
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player2, "Metathran Zombie");
    }

    @Test
    void redirectedKickedDamageDoesNotExileUntargetedCreature() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new ShamanEnKor());
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player2, indexOf(player2, shaman), null, titan.getId());
            harness.passBothPriorities();
        }

        castOnPermanent(true, shaman);

        harness.assertOnBattlefield(player2, "Shaman en-Kor");
        assertThat(shaman.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Kavu Titan");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Kavu Titan"));
    }

    @Test
    void redirectedKickedDamageAllowsUntargetedCreatureToRegenerate() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new ShamanEnKor());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new MetathranZombie());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, indexOf(player2, zombie), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, indexOf(player2, shaman), null, zombie.getId());
        harness.passBothPriorities();

        castOnPermanent(true, shaman);

        harness.assertOnBattlefield(player2, "Metathran Zombie");
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getMarkedDamage()).isZero();
        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Metathran Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Metathran Zombie"));
    }

    private void castOnCreature(boolean kicked) {
        castOnPermanent(kicked, gd.playerBattlefields.get(player2.getId()).getFirst());
    }

    private void castOnPermanent(boolean kicked, Permanent target) {
        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, kicked ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
            harness.passBothPriorities();
        } else {
            harness.castAndResolveInstant(player1, 0, target.getId());
        }
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
