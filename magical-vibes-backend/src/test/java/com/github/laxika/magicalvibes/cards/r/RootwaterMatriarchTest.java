package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RootwaterMatriarchTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Rootwater Matriarch has correct card properties")
    void hasCorrectProperties() {
        RootwaterMatriarch card = new RootwaterMatriarch();

        assertThat(card.getName()).isEqualTo("Rootwater Matriarch");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.MERFOLK);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GainControlOfEnchantedTargetEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new RootwaterMatriarch()));
        harness.addMana(player1, "U", 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Rootwater Matriarch");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new RootwaterMatriarch()));
        harness.addMana(player1, "U", 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rootwater Matriarch"));
    }

    // ===== Activated ability: stealing an enchanted creature =====

    @Test
    @DisplayName("Activating ability targeting enchanted creature puts it on the stack")
    void activatingTargetingEnchantedCreaturePutsOnStack() {
        Permanent matriarch = addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Rootwater Matriarch");
        assertThat(entry.getTargetPermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Activating ability taps Rootwater Matriarch")
    void activatingTapsMatriarch() {
        Permanent matriarch = addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(matriarch.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability steals enchanted creature")
    void resolvingStealsEnchantedCreature() {
        addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Creature should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Stolen creature should be summoning sick
        assertThat(creature.isSummoningSick()).isTrue();

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
        assertThat(gd.enchantmentDependentStolenCreatures).contains(creature.getId());
    }

    // ===== Not enchanted: ability does nothing =====

    @Test
    @DisplayName("Ability does nothing if target creature is not enchanted at resolution")
    void abilityDoesNothingIfNotEnchantedAtResolution() {
        addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        Permanent aura = attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove the aura before resolution
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.passBothPriorities();

        // Creature should still be on player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can target an unenchanted creature (but ability does nothing on resolution)")
    void canTargetUnenchantedCreature() {
        addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);

        // Targeting should succeed — "enchanted" is a duration, not a targeting restriction
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Creature should stay with player2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("not enchanted"));
    }

    // ===== Creature returns when no longer enchanted =====

    @Test
    @DisplayName("Creature returns to owner when aura is destroyed")
    void creatureReturnsWhenAuraDestroyed() {
        addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        // Steal the creature
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Find the Pacifism aura
        Permanent pacifismPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pacifism"))
                .findFirst().orElseThrow();

        // Destroy Pacifism with Demystify
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, "W", 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, pacifismPerm.getId());
        harness.passBothPriorities();

        // Creature should return to player2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Tracking should be cleaned up
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        assertThat(gd.enchantmentDependentStolenCreatures).doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("Creature stays stolen if a second aura remains after first is destroyed")
    void creatureStaysStolenIfSecondAuraRemains() {
        addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        // Steal the creature
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Attach a second aura after stealing
        attachAura(player2, creature, new Pacifism());

        // Find the first Pacifism aura (on player1's battlefield)
        Permanent pacifismPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pacifism"))
                .findFirst().orElseThrow();

        // Destroy first Pacifism with Demystify
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, "W", 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, pacifismPerm.getId());
        harness.passBothPriorities();

        // Creature should STILL be on player1's battlefield — second Pacifism is still attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
        assertThat(gd.enchantmentDependentStolenCreatures).contains(creature.getId());
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate ability when summoning sick")
    void cannotActivateWhenSummoningSick() {
        RootwaterMatriarch card = new RootwaterMatriarch();
        Permanent matriarch = new Permanent(card);
        matriarch.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(matriarch);

        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent matriarch = addReadyMatriarch(player1);
        matriarch.tap();
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyMatriarch(player1);
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Stealing adds to game log")
    void stealingAddsToGameLog() {
        addReadyMatriarch(player1);
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("gains control of") && log.contains("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReadyMatriarch(Player player) {
        RootwaterMatriarch card = new RootwaterMatriarch();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attachAura(Player player, Permanent target, Card auraCard) {
        Permanent auraPerm = new Permanent(auraCard);
        auraPerm.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player.getId()).add(auraPerm);
        return auraPerm;
    }
}
