package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasFury.class, GrizzlyBears.class, HillGiant.class, GarrukPrimalHunter.class, ArborElf.class})
class ChandrasFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player")
    void deals4DamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChandrasFury()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 1 damage to each creature the target player controls")
    void deals1DamageToEachCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasFury()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Does not damage the caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChandrasFury()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        List<Permanent> casterBattlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(casterBattlefield).hasSize(1);
        assertThat(casterBattlefield.getFirst().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damages a planeswalker and its controller's creatures")
    void damagesPlaneswalkerAndItsControllersCreatures() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukPrimalHunter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player2, new ArborElf());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChandrasFury()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Arbor Elf");
        harness.assertNotOnBattlefield(player2, "Arbor Elf");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Arbor Elf");
    }

    @Test
    @DisplayName("Still damages creatures when the targeted planeswalker receives lethal damage")
    void damagesCreaturesWhenPlaneswalkerReceivesLethalDamage() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukPrimalHunter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefield(player2, new ArborElf());
        harness.setHand(player1, List.of(new ChandrasFury()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        harness.assertInGraveyard(player2, "Garruk, Primal Hunter");
        harness.assertInGraveyard(player2, "Arbor Elf");
        harness.assertNotOnBattlefield(player2, "Arbor Elf");
    }

    @Test
    @DisplayName("Can target its caster and damages only that player's creatures")
    void canTargetItsCaster() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ArborElf());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new ChandrasFury()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertInGraveyard(player1, "Arbor Elf");
        harness.assertNotOnBattlefield(player1, "Arbor Elf");
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Arbor Elf");
    }

    @Test
    @DisplayName("Resolves when the target player controls no creatures")
    void worksWithNoCreatures() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChandrasFury()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
