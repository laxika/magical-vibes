package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlowMotion.class, GiantCockroach.class, GrimMonolith.class})
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

        harness.assertInGraveyard(player1, "Giant Cockroach");
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

    @Test
    @DisplayName("Resolving Slow Motion attaches it to a creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreature(player2);

        Permanent aura = castSlowMotionOn(creature);

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Slow Motion cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());
        harness.setHand(player1, List.of(new SlowMotion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Accepting the upkeep payment without enough mana sacrifices the creature")
    void acceptingWithoutEnoughManaSacrificesEnchantedCreature() {
        Permanent creature = addCreature(player2);
        attachSlowMotion(player1, creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Giant Cockroach");
    }

    @Test
    @DisplayName("Slow Motion does not trigger during the Aura controller's upkeep")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent creature = addCreature(player2);
        attachSlowMotion(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Slow Motion returns to its owner's hand when controlled by another player")
    void returnsToOwnerHandWhenControlledByOpponent() {
        Permanent creature = addCreature(player2);
        SlowMotion card = new SlowMotion();
        card.setOwnerId(player1.getId());
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        harness.assertNotInGraveyard(player1, "Slow Motion");
        harness.assertNotInHand(player2, "Slow Motion");
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GiantCockroach());
    }

    private Permanent attachSlowMotion(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new SlowMotion());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private Permanent castSlowMotionOn(Permanent creature) {
        harness.setHand(player1, List.of(new SlowMotion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Slow Motion");
        assertThat(aura).isNotNull();
        return aura;
    }
}
