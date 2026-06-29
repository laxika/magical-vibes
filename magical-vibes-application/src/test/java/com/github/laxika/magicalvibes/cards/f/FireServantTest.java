package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FireServantTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Fire Servant has correct static effect")
    void hasCorrectEffect() {
        FireServant card = new FireServant();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(DoubleControllerDamageEffect.class);
        DoubleControllerDamageEffect effect = (DoubleControllerDamageEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.stackFilter()).isNotNull();
        assertThat(effect.appliesToCombatDamage()).isFalse();
    }

    // ===== Doubles red instant damage =====

    @Test
    @DisplayName("Doubles Shock (red instant) damage to a player")
    void doublesRedInstantDamageToPlayer() {
        harness.addToBattlefield(player1, new FireServant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles Shock (red instant) damage to a creature")
    void doublesRedInstantDamageToCreature() {
        harness.addToBattlefield(player1, new FireServant());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, serraId);
        harness.passBothPriorities();

        // 2 damage doubled to 4 — kills Serra Angel (4/4)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    // ===== Doubles red sorcery damage =====

    @Test
    @DisplayName("Doubles Blaze (red sorcery) X damage to a player")
    void doublesRedSorceryDamageToPlayer() {
        harness.addToBattlefield(player1, new FireServant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    // ===== Does NOT double combat damage =====

    @Test
    @DisplayName("Does not double unblocked combat damage")
    void doesNotDoubleCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FireServant()); // 4/3

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(1)); // bear is at index 1 (Fire Servant at 0)

        // 2 combat damage — NOT doubled (combat damage is not from an instant/sorcery spell)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Does NOT double activated ability damage =====

    @Test
    @DisplayName("Does not double activated ability damage")
    void doesNotDoubleActivatedAbilityDamage() {
        harness.addToBattlefield(player1, new FireServant());
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        // 5 damage — NOT doubled (activated ability is not an instant/sorcery spell)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Only doubles controller's spells =====

    @Test
    @DisplayName("Does not double opponent's red spells")
    void doesNotDoubleOpponentsRedSpells() {
        harness.addToBattlefield(player1, new FireServant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // 2 damage — NOT doubled (opponent's spell, not Fire Servant controller's)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Two Fire Servants stack multiplicatively =====

    @Test
    @DisplayName("Two Fire Servants quadruple red spell damage")
    void twoFireServantsQuadrupleDamage() {
        harness.addToBattlefield(player1, new FireServant());
        harness.addToBattlefield(player1, new FireServant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 * 2 = 8 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    // ===== Fire Servant + Furnace of Rath =====

    @Test
    @DisplayName("Fire Servant stacks with Furnace of Rath for red spells")
    void stacksWithFurnaceOfRath() {
        harness.addToBattlefield(player1, new FireServant());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 (Furnace global) * 2 (Fire Servant spell) = 8 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    // ===== Removing Fire Servant stops doubling =====

    @Test
    @DisplayName("Removing Fire Servant from battlefield stops doubling")
    void removingStopsDoubling() {
        harness.addToBattlefield(player1, new FireServant());
        harness.setLife(player2, 20);

        // Deal doubled damage first
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Remove Fire Servant from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Fire Servant"));

        // Deal damage again without Fire Servant
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 damage (not doubled), life goes from 16 to 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    // ===== Helpers =====

    private Permanent addReadyInvoker(Player player) {
        FlamewaveInvoker card = new FlamewaveInvoker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
