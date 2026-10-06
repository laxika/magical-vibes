package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SixthSense.class, GrizzlyBears.class})
class SixthSenseTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature dealing combat damage presents may-draw choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSixthSense(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may-draw after combat damage draws a card")
    void acceptingMayDrawsCard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSixthSense(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining may-draw after combat damage does not draw a card")
    void decliningMayDoesNotDraw() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSixthSense(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when enchanted creature is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSixthSense(player1, creature);
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender still takes combat damage regardless of may choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSixthSense(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Grizzly Bears is 2/2, should deal 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Casting Sixth Sense attaches it to the target creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SixthSense()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sixth Sense")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Sixth Sense fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SixthSense()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sixth Sense");
        harness.assertNotOnBattlefield(player1, "Sixth Sense");
    }

    @Test
    @DisplayName("The creature controller chooses and draws when the Aura has a different controller")
    void creatureControllerReceivesDrawChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSixthSense(player2, creature);
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int creatureControllerHandSize = gd.playerHands.get(player1.getId()).size();
        int auraControllerHandSize = gd.playerHands.get(player2.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(creatureControllerHandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(auraControllerHandSize);
    }

    @Test
    @DisplayName("Damage by an unenchanted creature does not trigger Sixth Sense")
    void unenchantedAttackerDoesNotTrigger() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        attachSixthSense(player1, enchantedCreature);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void attachSixthSense(Player controller, Permanent creature) {
        Permanent sixthSensePerm = harness.addToBattlefieldAndReturn(controller, new SixthSense());
        sixthSensePerm.setAttachedTo(creature.getId());
    }
}
