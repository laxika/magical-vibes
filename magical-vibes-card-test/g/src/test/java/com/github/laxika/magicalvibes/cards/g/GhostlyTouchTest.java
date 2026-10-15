package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostlyTouch.class, WanderingWolf.class})
class GhostlyTouchTest extends BaseCardTest {

    private Permanent enchantAttacker() {
        Permanent attacker = addCreatureReady(player1, new WanderingWolf());
        Permanent aura = new Permanent(new GhostlyTouch());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return attacker;
    }

    @Test
    @DisplayName("Attacking with the enchanted creature queues target selection for the granted ability")
    void attackQueuesTargetSelection() {
        enchantAttacker();
        addCreatureReady(player2, new WanderingWolf());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting taps an untapped target permanent")
    void acceptTapsUntappedTarget() {
        enchantAttacker();
        Permanent victim = addCreatureReady(player2, new WanderingWolf());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting untaps a tapped target permanent")
    void acceptUntapsTappedTarget() {
        enchantAttacker();
        Permanent victim = addCreatureReady(player2, new WanderingWolf());
        victim.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves the target permanent untouched")
    void declineLeavesTargetUntouched() {
        enchantAttacker();
        Permanent victim = addCreatureReady(player2, new WanderingWolf());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An unenchanted attacker grants no trigger")
    void unenchantedAttackerDoesNotTrigger() {
        addCreatureReady(player1, new WanderingWolf());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GhostlyTouch()));
        addCreatureReady(player2, new WanderingWolf());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The enchanted creature controls the granted attack ability")
    void opponentsEnchantedCreatureControlsAbility() {
        Permanent attacker = addCreatureReady(player2, new WanderingWolf());
        Permanent aura = new Permanent(new GhostlyTouch());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        Permanent victim = addCreatureReady(player1, new WanderingWolf());

        declareAttackers(player2, List.of(0));

        PermanentChoiceContext.AttackTriggerTarget context =
                (PermanentChoiceContext.AttackTriggerTarget) gd.interaction.permanentChoiceContext();
        assertThat(context.choosingPlayerId()).isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, victim.getId());
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature is the source of the granted attack ability")
    void enchantedCreatureIsAbilitySource() {
        Permanent attacker = enchantAttacker();
        Permanent victim = addCreatureReady(player2, new WanderingWolf());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("The attack ability can untap its own attacker")
    void untapsItsOwnAttacker() {
        Permanent attacker = enchantAttacker();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack ability can tap a noncreature permanent")
    void tapsNoncreaturePermanent() {
        enchantAttacker();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).get(1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aura.isTapped()).isTrue();
    }
}
