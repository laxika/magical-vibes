package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.z.Zap;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrborgSkeleton.class, Zap.class})
class UrborgSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without a +1/+1 counter when not kicked")
    void entersWithoutCounterWhenNotKicked() {
        harness.setHand(player1, List.of(new UrborgSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Urborg Skeleton");
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter when kicked")
    void entersWithCounterWhenKicked() {
        harness.setHand(player1, List.of(new UrborgSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Urborg Skeleton");
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal damage")
    void regenerationShieldSavesItFromLethalDamage() {
        harness.addToBattlefield(player1, new UrborgSkeleton());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        java.util.UUID skeletonId = harness.getPermanentId(player1, "Urborg Skeleton");
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new UrborgSkeleton()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, skeletonId);

        Permanent skeleton = findPermanent(player1, "Urborg Skeleton");
        assertThat(skeleton).isNotNull();
        assertThat(skeleton.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Creating a regeneration shield does not tap the creature")
    void creatingShieldDoesNotTapCreature() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new UrborgSkeleton());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
        assertThat(skeleton.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A regeneration shield only saves the creature once")
    void shieldOnlySavesCreatureOnce() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new UrborgSkeleton());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Zap(), new Zap()));
        harness.setLibrary(player1, List.of(new UrborgSkeleton(), new UrborgSkeleton()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, skeleton.getId());

        harness.assertOnBattlefield(player1, "Urborg Skeleton");
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(skeleton.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player1, 0, skeleton.getId());

        harness.assertNotOnBattlefield(player1, "Urborg Skeleton");
        harness.assertInGraveyard(player1, "Urborg Skeleton");
    }

    @Test
    @DisplayName("Regeneration preserves the kicker counter and clears accumulated lethal damage")
    void regenerationPreservesKickerCounter() {
        harness.setHand(player1, List.of(new UrborgSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent skeleton = findPermanent(player1, "Urborg Skeleton");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Zap(), new Zap()));
        harness.setLibrary(player1, List.of(new UrborgSkeleton(), new UrborgSkeleton()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, skeleton.getId());

        assertThat(skeleton.getMarkedDamage()).isEqualTo(1);
        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
        assertThat(skeleton.isTapped()).isFalse();

        harness.castAndResolveInstant(player1, 0, skeleton.getId());

        harness.assertOnBattlefield(player1, "Urborg Skeleton");
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(skeleton.getMarkedDamage()).isZero();
        assertThat(skeleton.getRegenerationShield()).isZero();
        assertThat(skeleton.isTapped()).isTrue();
    }
}
