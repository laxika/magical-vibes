package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumbingDoseTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Numbing Dose has correct effects")
    void hasCorrectEffects() {
        NumbingDose card = new NumbingDose();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(AttachedCreatureDoesntUntapEffect.class);
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(EnchantedCreatureControllerLosesLifeEffect.class);
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target a creature with Numbing Dose")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can target an artifact with Numbing Dose")
    void canTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanentByName(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving Numbing Dose attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Numbing Dose")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Resolving Numbing Dose attaches it to target artifact")
    void resolvingAttachesToArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanentByName(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Numbing Dose")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Tapped creature with Numbing Dose does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent dosePerm = new Permanent(new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(dosePerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped artifact with Numbing Dose does not untap during controller's untap step")
    void enchantedArtifactDoesNotUntap() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanentByName(player2, "Fountain of Youth");
        artifact.tap();

        Permanent dosePerm = new Permanent(new NumbingDose());
        dosePerm.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(dosePerm);

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    // ===== Upkeep life loss =====

    @Test
    @DisplayName("Enchanted permanent's controller loses 1 life at their upkeep")
    void enchantedPermanentControllerLosesLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2);

        Permanent dosePerm = new Permanent(new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(dosePerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Life loss trigger fires during enchanted artifact controller's upkeep")
    void lifeLossTriggerFiresForArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanentByName(player2, "Fountain of Youth");

        Permanent dosePerm = new Permanent(new NumbingDose());
        dosePerm.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(dosePerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Life loss trigger does NOT fire during aura controller's upkeep")
    void lifeLossDoesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2);

        Permanent dosePerm = new Permanent(new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(dosePerm);

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

        Permanent dosePerm = new Permanent(new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(dosePerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Removal =====

    @Test
    @DisplayName("Creature can untap again after Numbing Dose is removed")
    void creatureUntapsAfterDoseRemoved() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent dosePerm = new Permanent(new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(dosePerm);

        // Remove Numbing Dose
        gd.playerBattlefields.get(player1.getId()).remove(dosePerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

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
}
