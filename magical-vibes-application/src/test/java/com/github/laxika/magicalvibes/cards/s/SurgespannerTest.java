package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Surgespanner.class, GoldmeadowDodger.class, Island.class})
class SurgespannerTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may pay {1}{U}. If you do,
    //  return target permanent to its owner's hand."

    @Test
    @DisplayName("Paying returns a target noncreature permanent to its owner's hand")
    void payReturnsNoncreaturePermanent() {
        Permanent surgespanner = harness.addToBattlefieldAndReturn(player1, new Surgespanner());
        harness.addToBattlefield(player2, new Island());
        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tap(surgespanner);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Paying returns a target creature to its owner's hand")
    void payReturnsCreature() {
        Permanent surgespanner = harness.addToBattlefieldAndReturn(player1, new Surgespanner());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldmeadowDodger());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tap(surgespanner);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(perm -> perm.getId().equals(creature.getId()));
        harness.assertInHand(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Declining returns nothing and spends no mana")
    void declineDoesNothing() {
        Permanent surgespanner = harness.addToBattlefieldAndReturn(player1, new Surgespanner());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tap(surgespanner);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new Surgespanner());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoldmeadowDodger());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The target is chosen before the payment decision")
    void targetIsChosenBeforePaymentDecision() {
        Permanent surgespanner = harness.addToBattlefieldAndReturn(player1, new Surgespanner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tap(surgespanner);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(target.getId());
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
