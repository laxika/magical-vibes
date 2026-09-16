package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrimsCure.class, Plains.class, FreshVolunteers.class})
class OrimsCureTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 4 damage to a target player")
    void preventsNextFourDamageToPlayer() {
        harness.setHand(player1, List.of(new OrimsCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Prevents the next 4 damage to a target creature")
    void preventsNextFourDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new OrimsCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getDamagePreventionShield()).isEqualTo(4);
    }

    @Test
    @DisplayName("Prevents up to 4 combat damage to the targeted player")
    void preventsUpToFourCombatDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OrimsCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent firstAttacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent secondAttacker = addCreatureReady(player1, new FreshVolunteers());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("May be cast for its alternate cost by tapping an untapped creature while controlling a Plains")
    void castsWithAlternateCost() {
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        UUID creatureId = creature.getId();
        harness.setHand(player1, List.of(new OrimsCure()));

        harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of(creatureId));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Alternate cost is unavailable without controlling a Plains")
    void alternateCostRequiresPlains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new OrimsCure()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost requires the caster to control the Plains")
    void alternateCostRequiresCasterToControlPlains() {
        harness.addToBattlefield(player2, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new OrimsCure()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost requires an untapped creature")
    void alternateCostRequiresUntappedCreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        creature.tap();
        harness.setHand(player1, List.of(new OrimsCure()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
