package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulParryTest extends BaseCardTest {

    @Test
    @DisplayName("Soul Parry has correct effects and targeting")
    void hasCorrectProperties() {
        SoulParry card = new SoulParry();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(PreventAllDamageByTargetCreatureEffect.class);
        assertThat(card.getMinTargets()).isEqualTo(1);
        assertThat(card.getMaxTargets()).isEqualTo(2);
    }

    @Test
    @DisplayName("Single target creature is prevented from dealing damage")
    void singleTargetPrevented() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(bear.getId());
    }

    @Test
    @DisplayName("Two target creatures are both prevented from dealing damage")
    void twoTargetsPrevented() {
        Permanent bear1 = addCreature(player2);
        Permanent bear2 = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear1.getId(), bear2.getId()));

        assertThat(gd.permanentsPreventedFromDealingDamage)
                .contains(bear1.getId(), bear2.getId());
    }

    @Test
    @DisplayName("Prevented creature deals no combat damage to player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Cast Soul Parry targeting the attacker
        harness.castAndResolveInstant(player1, 0, List.of(attacker.getId()));

        // Set up combat — attacker is unblocked
        attacker.setAttacking(true);
        resolveCombat(player2);

        // Player should not have taken damage
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevented creature deals no combat damage to blocking creature")
    void preventsCombatDamageToCreature() {
        Permanent attacker = addCreature(player2); // Grizzly Bears 2/2
        Permanent blocker = addCreature(player1);   // Grizzly Bears 2/2
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Cast Soul Parry targeting the attacker
        harness.castAndResolveInstant(player1, 0, List.of(attacker.getId()));

        // Set up combat with blocking
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        // Blocker should survive (attacker's damage prevented), attacker should take blocker's damage
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spell partially resolves when one of two targets is removed")
    void partiallyResolvesWhenOneTargetRemoved() {
        Permanent bear1 = addCreature(player2);
        Permanent bear2 = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(bear1.getId(), bear2.getId()));

        // Remove first target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(bear1);

        harness.passBothPriorities();

        // Only the remaining target should be prevented
        assertThat(gd.permanentsPreventedFromDealingDamage)
                .doesNotContain(bear1.getId())
                .contains(bear2.getId());
    }

    @Test
    @DisplayName("Spell fizzles when all targets are removed")
    void fizzlesWhenAllTargetsRemoved() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(bear.getId()));

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(bear.getId());

        // Advance past end of turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    @Test
    @DisplayName("Soul Parry goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Soul Parry"));
    }

    private Permanent addCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
