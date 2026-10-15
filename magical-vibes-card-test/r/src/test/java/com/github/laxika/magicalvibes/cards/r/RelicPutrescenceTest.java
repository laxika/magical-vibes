package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CopperhornScout;
import com.github.laxika.magicalvibes.cards.s.SilverMyr;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicPutrescence.class, RatchetBomb.class, CopperhornScout.class, SilverMyr.class})
class RelicPutrescenceTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Relic Putrescence targeting an artifact")
    void canTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RatchetBomb());
        harness.setHand(player1, List.of(new RelicPutrescence()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Relic Putrescence");
        assertThat(entry.getTargetId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Cannot cast Relic Putrescence targeting a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new RatchetBomb()); // valid target so spell is playable
        harness.addToBattlefield(player1, new CopperhornScout());
        Permanent creature = findPermanent(player1, "Copperhorn Scout");
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RatchetBomb());
        harness.setHand(player1, List.of(new RelicPutrescence()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Relic Putrescence")
                        && artifact.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Tapping enchanted artifact pushes Relic Putrescence trigger onto the stack")
    void tapTriggerPushesOntoStack() {
        addArtifactWithAura(player1, player2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Relic Putrescence");
        });
    }

    @Test
    @DisplayName("Relic Putrescence trigger goes on stack on top of the activated ability (resolves first)")
    void triggerGoesOnTopOfActivatedAbility() {
        addArtifactWithAura(player1, player2);

        harness.activateAbility(player1, 0, null, null);

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
        addArtifactWithAura(player1, player2);

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();

        harness.activateAbility(player1, 0, null, null);
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
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        // Untap and tap again
        artifact.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller of enchanted artifact gets the poison counter, not aura controller")
    void artifactControllerGetsPoisonNotAuraController() {
        // Player 1 controls the artifact, Player 2 controls the aura
        addArtifactWithAura(player1, player2);

        harness.activateAbility(player1, 0, null, null);
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
        addArtifactWithAura(player1, player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing aura stops the trigger")
    void removingAuraStopsTrigger() {
        addArtifactWithAura(player1, player2);

        // Remove the aura
        gd.playerBattlefields.get(player2.getId()).removeIf(
                p -> p.getCard().getName().equals("Relic Putrescence"));

        harness.activateAbility(player1, 0, null, null);

        // No Relic Putrescence trigger on the stack
        assertThat(gd.stack).noneMatch(
                entry -> entry.getCard().getName().equals("Relic Putrescence"));
    }

    @Test
    @DisplayName("Tapping un-enchanted artifact does not give a poison counter")
    void unenchantedArtifactNoTrigger() {
        harness.addToBattlefield(player1, new RatchetBomb());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).noneMatch(
                entry -> entry.getCard().getName().equals("Relic Putrescence"));
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Relic Putrescence trigger generates appropriate game log entries")
    void triggerGeneratesLogEntries() {
        addArtifactWithAura(player1, player2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Relic Putrescence") && log.contains("triggers"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("poison counter"));
    }

    @Test
    @DisplayName("Poison goes to the artifact controller when the trigger resolves")
    void controlChangeBeforeResolutionChangesPoisonRecipient() {
        Permanent artifact = addArtifactWithAura(player1, player2);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the aura after triggering does not stop the poison counter")
    void removingAuraAfterTriggerDoesNotStopPoison() {
        addArtifactWithAura(player1, player2);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player2.getId()).removeIf(
                p -> p.getCard() instanceof RelicPutrescence);

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Removing the artifact after triggering still gives its last controller poison")
    void removingArtifactAfterTriggerStillGivesPoison() {
        Permanent artifact = addArtifactWithAura(player1, player2);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanting an already tapped artifact does not trigger poison")
    void canEnchantTappedArtifactWithoutTriggering() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RatchetBomb());
        artifact.tap();
        harness.setHand(player1, List.of(new RelicPutrescence()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Relic Putrescence").getAttachedTo()).isEqualTo(artifact.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Tapping for mana adds mana immediately and puts poison on the stack")
    void manaAbilityResolvesBeforePoisonTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SilverMyr());
        artifact.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RelicPutrescence());
        aura.setAttachedTo(artifact.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    /**
     * Places a Ratchet Bomb on the artifact controller's battlefield and attaches
     * a Relic Putrescence controlled by the aura controller.
     *
     * @return the Ratchet Bomb permanent
     */
    private Permanent addArtifactWithAura(Player artifactController, Player auraController) {
        Permanent artifact = harness.addToBattlefieldAndReturn(artifactController, new RatchetBomb());

        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new RelicPutrescence());
        aura.setAttachedTo(artifact.getId());

        return artifact;
    }
}
