package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, skeletonId);
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Urborg Skeleton");
        assertThat(skeleton).isNotNull();
        assertThat(skeleton.getRegenerationShield()).isZero();
    }
}
