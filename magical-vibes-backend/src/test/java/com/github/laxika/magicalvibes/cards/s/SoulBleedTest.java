package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulBleedTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Soul Bleed has correct effects")
    void hasCorrectEffects() {
        SoulBleed card = new SoulBleed();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(EnchantedCreatureControllerLosesLifeEffect.class);
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target a creature with Soul Bleed")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new SoulBleed()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Soul Bleed")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = findPermanentByName(player1, "Fountain of Youth");

        harness.setHand(player1, List.of(new SoulBleed()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving Soul Bleed attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new SoulBleed()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Soul Bleed")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== Upkeep life loss =====

    @Test
    @DisplayName("Enchanted creature's controller loses 1 life at their upkeep")
    void enchantedCreatureControllerLosesLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2);

        Permanent auraPerm = new Permanent(new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Life loss trigger does NOT fire during aura controller's upkeep")
    void lifeLossDoesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2);

        Permanent auraPerm = new Permanent(new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1 (aura controller) should not lose life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life loss accumulates over multiple upkeeps")
    void lifeLossAccumulatesOverUpkeeps() {
        Permanent creature = addCreatureReady(player2);

        Permanent auraPerm = new Permanent(new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Removal =====

    @Test
    @DisplayName("No life loss after Soul Bleed is removed")
    void noLifeLossAfterRemoval() {
        Permanent creature = addCreatureReady(player2);

        Permanent auraPerm = new Permanent(new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Remove Soul Bleed
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    // ===== Helpers =====

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
