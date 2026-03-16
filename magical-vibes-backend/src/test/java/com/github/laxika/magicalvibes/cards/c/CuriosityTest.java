package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CuriosityTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Curiosity has correct effects")
    void hasCorrectEffects() {
        Curiosity card = new Curiosity();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Enchanted creature dealing combat damage presents may-draw choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may-draw after combat damage draws a card")
    void acceptingMayDrawsCard() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining may-draw after combat damage does not draw a card")
    void decliningMayDoesNotDraw() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when enchanted creature is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        creature.setAttacking(true);

        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Defender still takes combat damage regardless of may choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Grizzly Bears is 2/2, should deal 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Casting and attachment =====

    @Test
    @DisplayName("Casting Curiosity attaches it to the target creature")
    void castingAttachesToCreature() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Curiosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curiosity")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Curiosity fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Curiosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Curiosity"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Curiosity"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void attachCuriosity(Player controller, Permanent creature) {
        Permanent curiosityPerm = new Permanent(new Curiosity());
        curiosityPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(curiosityPerm);
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
