package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.v.VenomSliver;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConstrictingSliver.class, VenomSliver.class, RuneclawBear.class, LightningStrike.class, Cloudshift.class})
class ConstrictingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver entering under your control gets the optional exile trigger")
    void sliverGetsGrantedExileTrigger() {
        harness.addToBattlefield(player1, new ConstrictingSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new VenomSliver()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Runeclaw Bear"));
    }

    @Test
    @DisplayName("A non-Sliver entering under your control does not get the exile trigger")
    void nonSliverDoesNotGetGrantedExileTrigger() {
        harness.addToBattlefield(player1, new ConstrictingSliver());
        harness.addToBattlefield(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The granted trigger only offers creatures controlled by opponents")
    void grantedTriggerOnlyTargetsOpponentCreatures() {
        harness.addToBattlefield(player1, new ConstrictingSliver());
        harness.addToBattlefield(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new VenomSliver()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void constrictingSliverTriggersForItsOwnEntry() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new ConstrictingSliver()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void controllerCanDeclineExile() {
        harness.addToBattlefield(player1, new ConstrictingSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        enterVenomAndChooseTarget(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentSliverDoesNotReceiveGrantedAbility() {
        harness.addToBattlefield(player1, new ConstrictingSliver());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.enterBattlefieldAndReturn(player2, new VenomSliver());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exileLastsUntilEnteringSliverLeavesEvenAfterGrantingSliverLeaves() {
        Permanent grantingSliver = harness.addToBattlefieldAndReturn(player1, new ConstrictingSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        enterVenomAndChooseTarget(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");

        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, grantingSliver.getId());
        harness.assertInGraveyard(player1, "Constricting Sliver");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Venom Sliver"));
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringSliverLeavingBeforeResolutionPreventsExile() {
        harness.addToBattlefield(player1, new ConstrictingSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        enterVenomAndChooseTarget(target);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Venom Sliver"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Venom Sliver");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingGrantingSliverDoesNotStopAlreadyTriggeredExile() {
        Permanent grantingSliver = harness.addToBattlefieldAndReturn(player1, new ConstrictingSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        enterVenomAndChooseTarget(target);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, grantingSliver.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Constricting Sliver");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @CardUsed({Cloudshift.class})
    void returningSliverIsNotTheSourceOfItsOldExileTrigger() {
        harness.addToBattlefield(player1, new ConstrictingSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        enterVenomAndChooseTarget(target);
        var originalSliverId = harness.getPermanentId(player1, "Venom Sliver");
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, originalSliverId);

        assertThat(harness.getPermanentId(player1, "Venom Sliver")).isNotEqualTo(originalSliverId);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void enterVenomAndChooseTarget(Permanent target) {
        harness.setHand(player1, List.of(new VenomSliver()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
    }
}
