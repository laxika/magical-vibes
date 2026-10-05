package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.t.TeferiHeroOfDominaria;
import com.github.laxika.magicalvibes.cards.m.MesaUnicorn;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.c.ColdWaterSnapper;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiatingLightning.class, MesaUnicorn.class, BalothGorger.class,
        TeferiHeroOfDominaria.class, LlanowarElves.class, ColdWaterSnapper.class})
class RadiatingLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 1 damage to each creature target player controls")
    void deals1DamageToEachCreature() {
        harness.setLife(player2, 20);
        // Mesa Unicorn is 2/2 — survives 1 damage
        harness.addToBattlefield(player2, new MesaUnicorn());
        harness.addToBattlefield(player2, new MesaUnicorn());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Both Mesa Unicorns should survive with 1 damage
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 1);
        // Radiating Lightning has no "attacks this turn if able" rider — Aggravate's flag must not
        // ride along on every card sharing the effect.
        assertThat(battlefield).noneMatch(Permanent::isMustAttackThisTurn);
    }

    @Test
    @DisplayName("Kills 1-toughness creatures")
    void kills1ToughnessCreatures() {
        harness.setLife(player2, 20);
        // Add a 1/1 creature to player2
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaves the target player's planeswalker alone — only creatures are damaged")
    void leavesTheTargetPlayersPlaneswalkerAlone() {
        harness.setLife(player2, 20);
        Permanent teferi = harness.addToBattlefieldAndReturn(player2, new TeferiHeroOfDominaria());
        teferi.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player2, new MesaUnicorn());

        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // The Unicorn takes its 1 damage; the planeswalker keeps every loyalty counter.
        assertThat(findPermanent(player2, "Mesa Unicorn").getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Teferi, Hero of Dominaria");
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not damage caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        List<Permanent> casterBattlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(casterBattlefield).hasSize(1);
        assertThat(casterBattlefield.getFirst().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals 3 to player and 1 to each creature simultaneously")
    void dealsBothDamages() {
        harness.setLife(player2, 20);
        // Baloth Gorger is 4/4 — survives 1 damage
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addToBattlefield(player2, new MesaUnicorn());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player takes 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Both creatures survive with 1 damage each
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Works when target player controls no creatures")
    void worksWithNoCreatures() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target its controller and damages only that player's creatures")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MesaUnicorn());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new MesaUnicorn());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damages creatures present at resolution, including those added after casting")
    void damagesCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MesaUnicorn());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Radiating Lightning");
    }

    @Test
    @DisplayName("Damages hexproof creatures because only their controller is targeted")
    void damagesHexproofCreatures() {
        harness.setLife(player2, 20);
        Permanent snapper = harness.addToBattlefieldAndReturn(player2, new ColdWaterSnapper());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(snapper.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Cold-Water Snapper");
    }
}
