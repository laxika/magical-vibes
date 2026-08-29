package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlowMotionTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature's controller may pay {2} to keep it")
    void enchantedControllerMayPayToKeepCreature() {
        Permanent creature = addCreature(player2);
        attachSlowMotion(player1, creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The enchanted creature is sacrificed when its controller cannot pay")
    void sacrificesEnchantedCreatureWhenControllerCannotPay() {
        Permanent creature = addCreature(player1);
        attachSlowMotion(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Slow Motion");
    }

    @Test
    @DisplayName("Slow Motion returns to its owner's hand when put into a graveyard")
    void returnsToHandFromGraveyard() {
        Permanent creature = addCreature(player1);
        Permanent aura = attachSlowMotion(player1, creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Slow Motion");
        harness.assertNotInGraveyard(player1, "Slow Motion");
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent attachSlowMotion(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new SlowMotion());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
