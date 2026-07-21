package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();

        Permanent aura = new Permanent(new SavingGrace());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Damage that would be dealt to you is dealt to the enchanted creature instead")
    void damageToControllerRedirectedToEnchantedCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        enchantAndResolve(bears);

        // Opponent shocks player1 for 2 — the damage is dealt to the enchanted creature instead.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(bears.getMarkedDamage()).isEqualTo(2); // 2/5, survives
    }

    @Test
    @DisplayName("Damage that would be dealt to a permanent you control is dealt to the enchanted creature instead")
    void damageToYourPermanentRedirectedToEnchantedCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // enchanted creature
        harness.addToBattlefield(player1, new GrizzlyBears()); // other creature you control
        Permanent enchanted = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent other = gd.playerBattlefields.get(player1.getId()).get(1);
        enchantAndResolve(enchanted);

        // Opponent shocks the OTHER creature — the damage is redirected onto the enchanted creature.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, other.getId());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isEqualTo(0);
        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirection wears off at end of turn")
    void redirectionWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Saving Grace"));
    }
}
