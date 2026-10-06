package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Blastoderm;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SivvisRuse.class, GrizzlyBears.class, Blastoderm.class, SkyshroudRidgeback.class,
        SealOfFire.class, Mountain.class, Plains.class})
class SivvisRuseTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when an opponent controls a Mountain and you control a Plains")
    void castsForFreeWithRequiredLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SivvisRuse()));

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sivvi's Ruse");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be cast for free when only the controller controls a Mountain")
    void cannotCastForFreeWithoutRequiredLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SivvisRuse()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast for free without a Plains you control")
    void cannotCastForFreeWithoutControllerPlains() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SivvisRuse()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally when the free-cast condition is not met")
    void castsNormallyWithoutRequiredLands() {
        harness.setHand(player1, List.of(new SivvisRuse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Sivvi's Ruse");
    }

    @Test
    @DisplayName("Prevents damage to creatures you control")
    void preventsDamageToControlledCreatures() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SivvisRuse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player2, new Blastoderm());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent damage to the controller")
    void doesNotPreventDamageToController() {
        harness.setHand(player1, List.of(new SivvisRuse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player2, new Blastoderm());
        attacker.setAttacking(true);

        harness.setLife(player1, 20);
        resolveCombat(player2);

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Prevents repeated noncombat damage, including to a creature entering after resolution")
    void preventsNoncombatDamageToLaterCreature() {
        harness.setHand(player1, List.of(new SivvisRuse(), new SkyshroudRidgeback()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Skyshroud Ridgeback");
        harness.addToBattlefield(player2, new SealOfFire());
        harness.addToBattlefield(player2, new SealOfFire());

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Ridgeback");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage dealt by your creatures to opposing creatures")
    void doesNotPreventDamageToOpposingCreatures() {
        Permanent attacker = addCreatureReady(player1, new Blastoderm());
        Permanent blocker = addCreatureReady(player2, new SkyshroudRidgeback());
        harness.setHand(player1, List.of(new SivvisRuse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat(player1);

        harness.assertInGraveyard(player2, "Skyshroud Ridgeback");
        harness.assertOnBattlefield(player1, "Blastoderm");
        assertThat(attacker.getMarkedDamage()).isZero();
    }
}
