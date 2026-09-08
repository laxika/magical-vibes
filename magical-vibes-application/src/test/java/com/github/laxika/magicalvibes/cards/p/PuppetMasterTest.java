package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PuppetMasterTest extends BaseCardTest {

    private void attachPuppetMaster(Permanent enchanted) {
        Permanent aura = new Permanent(new PuppetMaster());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    @Test
    @DisplayName("Returns the enchanted creature, then paying {U}{U}{U} returns Puppet Master")
    void paysToReturnAuraAfterCreatureReturns() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attachPuppetMaster(bears);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Puppet Master");
        harness.assertNotInGraveyard(player1, "Puppet Master");
    }

    @Test
    @DisplayName("Declining the payment leaves Puppet Master in its owner's graveyard")
    void decliningPaymentLeavesAuraInGraveyard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attachPuppetMaster(bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Puppet Master");
        harness.assertNotInHand(player1, "Puppet Master");
    }
}
