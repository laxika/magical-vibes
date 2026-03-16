package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GiveEnchantedPermanentControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelicPutrescenceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Relic Putrescence has correct card properties")
    void hasCorrectProperties() {
        RelicPutrescence card = new RelicPutrescence();

        assertThat(card.isAura()).isTrue();
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED).getFirst())
                .isInstanceOf(GiveEnchantedPermanentControllerPoisonCountersEffect.class);
        GiveEnchantedPermanentControllerPoisonCountersEffect effect =
                (GiveEnchantedPermanentControllerPoisonCountersEffect) card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Relic Putrescence targeting an artifact")
    void canTargetArtifact() {
        harness.addToBattlefield(player1, new RatchetBomb());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new RelicPutrescence()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Relic Putrescence");
        assertThat(entry.getTargetPermanentId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Cannot cast Relic Putrescence targeting a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new RatchetBomb()); // valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new RelicPutrescence()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Resolving Relic Putrescence attaches it to target artifact")
    void resolvingAttachesToTargetArtifact() {
        harness.addToBattlefield(player1, new RatchetBomb());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new RelicPutrescence()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Relic Putrescence")
                        && artifact.getId().equals(p.getAttachedTo()));
    }

    // ===== Tap trigger =====

    @Test
    @DisplayName("Tapping enchanted artifact pushes Relic Putrescence trigger onto the stack")
    void tapTriggerPushesOntoStack() {
        Permanent artifact = addArtifactWithAura(player1, player2);

        gs.activateAbility(gd, player1, 0, 0, null, null, null);

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Relic Putrescence");
        });
    }

    @Test
    @DisplayName("Relic Putrescence trigger goes on stack on top of the activated ability (resolves first)")
    void triggerGoesOnTopOfActivatedAbility() {
        Permanent artifact = addArtifactWithAura(player1, player2);

        gs.activateAbility(gd, player1, 0, 0, null, null, null);

        assertThat(gd.stack).hasSize(2);
        // Activated ability should be on the bottom (first)
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        // Trigger should be on top (last, resolves first)
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Relic Putrescence");
    }

    @Test
    @DisplayName("Tapping enchanted artifact gives its controller a poison counter")
    void tappingEnchantedArtifactGivesPoisonCounter() {
        Permanent artifact = addArtifactWithAura(player1, player2);

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();

        gs.activateAbility(gd, player1, 0, 0, null, null, null);
        // Resolve both the activated ability and the triggered ability
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple taps accumulate poison counters")
    void multipleTapsAccumulatePoisonCounters() {
        Permanent artifact = addArtifactWithAura(player1, player2);

        // First tap
        gs.activateAbility(gd, player1, 0, 0, null, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        // Untap and tap again
        artifact.untap();
        gs.activateAbility(gd, player1, 0, 0, null, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    // ===== Controller of artifact gets the poison counter =====

    @Test
    @DisplayName("Controller of enchanted artifact gets the poison counter, not aura controller")
    void artifactControllerGetsPoisonNotAuraController() {
        // Player 1 controls the artifact, Player 2 controls the aura
        Permanent artifact = addArtifactWithAura(player1, player2);

        gs.activateAbility(gd, player1, 0, 0, null, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player 1 (artifact controller) gets the poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        // Player 2 (aura controller) does NOT get a poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Controller gets poison even when aura is on their own artifact")
    void ownArtifactStillGivesPoison() {
        // Player 1 controls both the artifact and the aura
        Permanent artifact = addArtifactWithAura(player1, player1);

        gs.activateAbility(gd, player1, 0, 0, null, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    // ===== No trigger when aura removed =====

    @Test
    @DisplayName("Removing aura stops the trigger")
    void removingAuraStopsTrigger() {
        Permanent artifact = addArtifactWithAura(player1, player2);

        // Remove the aura
        gd.playerBattlefields.get(player2.getId()).removeIf(
                p -> p.getCard().getName().equals("Relic Putrescence"));

        gs.activateAbility(gd, player1, 0, 0, null, null, null);

        // No Relic Putrescence trigger on the stack
        assertThat(gd.stack).noneMatch(
                entry -> entry.getCard().getName().equals("Relic Putrescence"));
    }

    // ===== No trigger for un-enchanted artifact =====

    @Test
    @DisplayName("Tapping un-enchanted artifact does not give a poison counter")
    void unenchantedArtifactNoTrigger() {
        harness.addToBattlefield(player1, new RatchetBomb());

        gs.activateAbility(gd, player1, 0, 0, null, null, null);

        assertThat(gd.stack).noneMatch(
                entry -> entry.getCard().getName().equals("Relic Putrescence"));
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Relic Putrescence trigger generates appropriate game log entries")
    void triggerGeneratesLogEntries() {
        Permanent artifact = addArtifactWithAura(player1, player2);

        gs.activateAbility(gd, player1, 0, 0, null, null, null);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Relic Putrescence") && log.contains("triggers"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("poison counter"));
    }

    // ===== Helpers =====

    /**
     * Places a Ratchet Bomb on the artifact controller's battlefield and attaches
     * a Relic Putrescence controlled by the aura controller.
     *
     * @return the Ratchet Bomb permanent
     */
    private Permanent addArtifactWithAura(Player artifactController, Player auraController) {
        harness.addToBattlefield(artifactController, new RatchetBomb());
        Permanent artifact = gd.playerBattlefields.get(artifactController.getId()).getFirst();

        RelicPutrescence auraCard = new RelicPutrescence();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);

        return artifact;
    }
}
