package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunfireTorch.class, GrizzlyBears.class})
class SunfireTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachTorch(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking can sacrifice the Torch and deal 2 damage after choosing the reflexive target")
    void attackTriggerSacrificesTorchAndDealsDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent torch = attachTorch(player1, bears);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, torch.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MayAbilityTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sunfire Torch");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Declining the attack trigger keeps the Torch attached")
    void decliningKeepsTorchAttached() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent torch = attachTorch(player1, bears);

        declareAttackers(player1, List.of(0));
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sunfire Torch");
        assertThat(torch.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent attachTorch(Player player, Permanent host) {
        Permanent torch = new Permanent(new SunfireTorch());
        torch.setSummoningSick(false);
        torch.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(torch);
        return torch;
    }
}
