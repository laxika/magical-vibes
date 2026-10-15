package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OgreMenial;
import com.github.laxika.magicalvibes.cards.o.ObNixilisTheAdversary;
import com.github.laxika.magicalvibes.cards.d.DragonMantle;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SchemingFence.class, OgreMenial.class, ObNixilisTheAdversary.class,
        DragonMantle.class, WitnessProtection.class})
class SchemingFenceTest extends BaseCardTest {

    @Test
    void choosesAnExistingNonlandPermanentAndGainsItsNonLoyaltyActivatedAbilities() {
        Permanent ogre = addCreatureReady(player1, new OgreMenial());
        Permanent fence = castFence();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ogre.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());

        harness.handlePermanentChosen(player1, ogre.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fence)).isEqualTo(3);
    }

    @Test
    void preventsTheChosenPermanentFromActivatingItsAbilities() {
        Permanent ogre = addCreatureReady(player1, new OgreMenial());
        castFence();
        harness.handlePermanentChosen(player1, ogre.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                   .isInstanceOf(IllegalStateException.class)
                   .hasMessageContaining("can't be activated");
    }

    @Test
    void mayDeclineToChooseAPermanent() {
        addCreatureReady(player1, new OgreMenial());
        Permanent fence = castFence();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(fence.getChosenPermanentId()).isNull();
    }

    @Test
    void entersWithoutChoosingWhenThereAreNoOtherPermanents() {
        Permanent fence = castFence();

        assertThat(fence.getChosenPermanentId()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGainLoyaltyAbilitiesButStillDisablesThem() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ObNixilisTheAdversary());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        castFence();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void losesGainedAbilitiesWhenTheChosenPermanentLeaves() {
        Permanent ogre = addCreatureReady(player1, new OgreMenial());
        castFence();
        harness.handlePermanentChosen(player1, ogre.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(ogre);
        harness.setGraveyard(player1, List.of(ogre.getCard()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void chosenPermanentCanActivateAgainAfterFenceLeaves() {
        Permanent ogre = addCreatureReady(player1, new OgreMenial());
        Permanent fence = castFence();
        harness.handlePermanentChosen(player1, ogre.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(fence);
        harness.setGraveyard(player1, List.of(fence.getCard()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(1);
    }

    @Test
    void chosenPermanentCanActivateWhenFenceLosesAllAbilities() {
        Permanent ogre = addCreatureReady(player1, new OgreMenial());
        Permanent fence = castFence();
        harness.handlePermanentChosen(player1, ogre.getId());
        harness.passBothPriorities();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        aura.setAttachedTo(fence.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(1);
    }

    @Test
    void unrelatedGrantedAbilityStillRequiresItsActualManaColor() {
        Permanent fence = castFence();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DragonMantle());
        aura.setAttachedTo(fence.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castFence() {
        harness.castFromHand(player1, new SchemingFence(), "{W}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Scheming Fence");
    }
}
