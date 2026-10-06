package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RuinRat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavingGrace.class, GrizzlyBears.class, Shock.class, Naturalize.class, RuinRat.class})
class SavingGraceTest extends BaseCardTest {

    /** Casts Saving Grace from player1's hand onto {@code creature} and resolves the spell + its enters trigger. */
    private void enchantAndResolve(Permanent creature) {
        harness.setHand(player1, List.of(new SavingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // aura resolves + attaches; enters trigger goes on the stack
        harness.passBothPriorities(); // enters trigger resolves; redirect shield installed
    }

    @Test
    @DisplayName("Enchanted creature gets +0/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SavingGrace());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Damage that would be dealt to you is dealt to the enchanted creature instead")
    void damageToControllerRedirectedToEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(bears);

        // Opponent shocks player1 for 2 — the damage is dealt to the enchanted creature instead.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(bears.getMarkedDamage()).isEqualTo(2); // 2/5, survives
    }

    @Test
    @DisplayName("Damage that would be dealt to a permanent you control is dealt to the enchanted creature instead")
    void damageToYourPermanentRedirectedToEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(enchanted);

        // Opponent shocks the OTHER creature — the damage is redirected onto the enchanted creature.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, other.getId());

        assertThat(other.getMarkedDamage()).isEqualTo(0);
        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirection wears off at end of turn")
    void redirectionWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(bears);
        assertThat(gd.turnDamageRedirectToCreatureShields).isNotEmpty();

        // Advance to the cleanup step — turn-scoped effects wear off.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.turnDamageRedirectToCreatureShields).isEmpty();
    }

    @Test
    @DisplayName("Cannot enchant a creature you don't control")
    void cannotEnchantOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        // No creature you control to enchant → the Aura has no legal target and can't be cast.
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Saving Grace");
    }

    @Test
    @DisplayName("Removing the Aura in response to its enters trigger still redirects damage")
    void auraRemovedBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Saving Grace"));
        harness.assertInGraveyard(player1, "Saving Grace");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Redirection persists after the Aura leaves following trigger resolution")
    void auraRemovedAfterTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(bears);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Saving Grace"));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Redirection stops once its destination creature has died")
    void destinationDiesThenDamageHitsPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(bears);
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 4);
        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player2, 0, player1.getId());
        }
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The redirection does not protect the opponent")
    void opponentDamageIsNotRedirected() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(bears);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Redirected combat damage retains its source's deathtouch")
    void redirectedCombatDamageRetainsDeathtouch() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(bears);
        addCreatureReady(player2, new RuinRat());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ruin Rat");
    }

    @Test
    @DisplayName("Flash allows Saving Grace to redirect a spell already on the stack")
    void flashInResponseToDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        enchantAndResolve(bears);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The affected player chooses between multiple Saving Grace destinations")
    void multipleAurasAllowChoosingDamageDestination() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantAndResolve(first);
        enchantAndResolve(second);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, second.getId());
        harness.assertLife(player1, 20);
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }
}
