package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.cards.r.RumblingAftershocks;
import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CometStorm.class, GrizzlyBears.class, GnarlidPack.class, RumblingAftershocks.class,
        JaceTheMindSculptor.class})
class CometStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to its single target without multikicker")
    void dealsDamageToSingleTarget() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstantForX(player1, 0, 3, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals the full X damage to every multikicked target")
    void dealsFullDamageToEachTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantForX(player1, 0, 2, List.of(bears.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Rejects choosing the same target more than once")
    void rejectsDuplicateTargets() {
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 2, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    void allowsZeroDamageWithMultipleTargets() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantForX(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Comet Storm");
    }

    @Test
    void paysForTwoExtraTargetsAndDealsFullDamageToEach() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantForX(player1, 0, 2,
                List.of(creature.getId(), player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gnarlid Pack");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void rejectsExtraTargetWithoutManaForMultikicker() {
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void explicitMultikickerPaymentsChargeManaOnlyOnce() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(creature.getId(), player1.getId(), player2.getId()), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{1}", "{1}"), false);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Gnarlid Pack");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void explicitMultikickerPaymentsMustMatchAdditionalTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 2, null, null,
                List.of(creature.getId(), player1.getId(), player2.getId()), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{1}"), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional target");
        harness.assertInHand(player1, "Comet Storm");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
    }

    @Test
    void multikickerTriggersRumblingAftershocksWithTheKickCount() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantForX(player1, 0, 2,
                List.of(creature.getId(), player1.getId(), player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void allowsMoreThanOneHundredDistinctTargets() {
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new GnarlidPack()).getId());
        }
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 102);

        harness.castInstantForX(player1, 0, 2, targets);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gnarlid Pack");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof GnarlidPack).hasSize(101);
    }

    @Test
    void dealsDamageToTargetPlaneswalker() {
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantForX(player1, 0, 2, List.of(jace.getId()));
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void stillDealsFullDamageToRemainingLegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstantForX(player1, 0, 2, List.of(creature.getId(), player2.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player2, "Gnarlid Pack");
        harness.assertNotInGraveyard(player2, "Gnarlid Pack");
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantForX(player1, 0, 2, List.of(creature.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Gnarlid Pack");
        harness.assertNotInGraveyard(player2, "Gnarlid Pack");
        harness.assertInGraveyard(player1, "Comet Storm");
    }
}
