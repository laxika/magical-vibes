package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MarketwatchPhantom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrovodHaunch.class, MarketwatchPhantom.class})
class KrovodHaunchTest extends BaseCardTest {

    @Test
    void equippingBoostsCreatureByTwoPower() {
        Permanent haunch = harness.addToBattlefieldAndReturn(player1, new KrovodHaunch());
        Permanent creature = addCreatureReady(player1, new MarketwatchPhantom());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(haunch.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void sacrificingItGainsThreeLife() {
        harness.addToBattlefield(player1, new KrovodHaunch());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Krovod Haunch");
    }

    @Test
    void payingWhenPutIntoGraveyardCreatesTwoDogs() {
        Permanent haunch = harness.addToBattlefieldAndReturn(player1, new KrovodHaunch());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, haunch));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Dog")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void decliningWhenPutIntoGraveyardCreatesNoDogs() {
        Permanent haunch = harness.addToBattlefieldAndReturn(player1, new KrovodHaunch());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, haunch));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Dog")).isEmpty();
    }

    @Test
    void sacrificingItCanAlsoPayToCreateDogsBeforeGainingLife() {
        harness.addToBattlefield(player1, new KrovodHaunch());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Krovod Haunch");
        harness.assertInGraveyard(player1, "Krovod Haunch");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Dog")).hasSize(2).allSatisfy(dog -> {
            assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(1);
        });
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void lastControllerPaysAndCreatesDogsEvenWhenOpponentOwnsHaunch() {
        KrovodHaunch card = new KrovodHaunch();
        card.setOwnerId(player2.getId());
        Permanent haunch = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, haunch));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Krovod Haunch");
        harness.assertNotInGraveyard(player1, "Krovod Haunch");
        assertThat(findPermanents(player1, "Dog")).hasSize(2);
        assertThat(findPermanents(player2, "Dog")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void returningItToHandDoesNotTriggerDogs() {
        Permanent haunch = harness.addToBattlefieldAndReturn(player1, new KrovodHaunch());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, haunch));

        harness.assertInHand(player1, "Krovod Haunch");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Dog")).isEmpty();
    }
}
