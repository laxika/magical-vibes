package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowingKnifeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachKnife(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature and accepting sacrifices the knife for 2 damage to a player")
    void attackTriggerDamagesPlayer() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachKnife(player1, bears);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Throwing Knife");
        harness.assertInGraveyard(player1, "Throwing Knife");
        // 2 from the knife, then 2 unblocked combat damage from the now-unequipped Bears.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The sacrificed knife can instead deal its 2 damage to a creature")
    void attackTriggerDamagesCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachKnife(player1, bears);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Throwing Knife");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may leaves the knife attached and deals no damage")
    void decliningKeepsKnife() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = attachKnife(player1, bears);

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Throwing Knife");
        assertThat(knife.getAttachedTo()).isEqualTo(bears.getId());
        // Only the equipped Bears' 4 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The knife does not trigger when its equipped creature stays home")
    void noTriggerWithoutAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        attachKnife(player1, bears);

        declareAttackers(player1, List.of(1));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Throwing Knife");
        // Only the unequipped attacker's 2 combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent attachKnife(Player player, Permanent host) {
        Permanent knife = new Permanent(new ThrowingKnife());
        knife.setSummoningSick(false);
        knife.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(knife);
        return knife;
    }
}
