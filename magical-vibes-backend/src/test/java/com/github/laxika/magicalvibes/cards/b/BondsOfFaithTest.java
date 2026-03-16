package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BondsOfFaithTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Bonds of Faith has correct effects")
    void hasCorrectProperties() {
        BondsOfFaith card = new BondsOfFaith();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        var effect = (EnchantedCreatureSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(effect.ifMatch()).isInstanceOf(StaticBoostEffect.class);
        assertThat(effect.ifNotMatch()).isInstanceOf(EnchantedCreatureCantAttackOrBlockEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Bonds of Faith attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bonds of Faith")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Human gets +2/+2 =====

    @Test
    @DisplayName("Human creature enchanted with Bonds of Faith gets +2/+2")
    void humanCreatureGetsBoost() {
        Permanent humanPerm = new Permanent(new HonorGuard());
        humanPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(humanPerm);

        // Attach Bonds of Faith directly
        Permanent bondsPerm = new Permanent(new BondsOfFaith());
        bondsPerm.setAttachedTo(humanPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(bondsPerm);

        // Honor Guard is 1/1; with +2/+2 should be 3/3
        assertThat(gqs.getEffectivePower(gd, humanPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, humanPerm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Human creature enchanted with Bonds of Faith can still attack")
    void humanCreatureCanAttack() {
        Permanent humanPerm = new Permanent(new HonorGuard());
        humanPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(humanPerm);

        Permanent bondsPerm = new Permanent(new BondsOfFaith());
        bondsPerm.setAttachedTo(humanPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(bondsPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Human creature should be able to attack — call succeeding proves it
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Human creature enchanted with Bonds of Faith can still block")
    void humanCreatureCanBlock() {
        Permanent humanPerm = new Permanent(new HonorGuard());
        humanPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(humanPerm);

        Permanent bondsPerm = new Permanent(new BondsOfFaith());
        bondsPerm.setAttachedTo(humanPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(bondsPerm);

        // Player1 has an attacker
        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Human creature should be able to block
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(humanPerm.isBlocking()).isTrue();
    }

    // ===== Non-Human can't attack or block =====

    @Test
    @DisplayName("Non-Human creature enchanted with Bonds of Faith does not get +2/+2")
    void nonHumanCreatureDoesNotGetBoost() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent bondsPerm = new Permanent(new BondsOfFaith());
        bondsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(bondsPerm);

        // Grizzly Bears is 2/2, should remain 2/2 (no boost for non-Human)
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Human creature enchanted with Bonds of Faith cannot attack")
    void nonHumanCreatureCannotAttack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent bondsPerm = new Permanent(new BondsOfFaith());
        bondsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(bondsPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Non-Human creature enchanted with Bonds of Faith cannot block")
    void nonHumanCreatureCannotBlock() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent bondsPerm = new Permanent(new BondsOfFaith());
        bondsPerm.setAttachedTo(blockerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(bondsPerm);

        // Player1 has an attacker (index 1, after Bonds at index 0)
        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    // ===== Bonds of Faith removed restores ability =====

    @Test
    @DisplayName("Non-Human creature can attack again after Bonds of Faith is removed")
    void nonHumanCanAttackAfterBondsRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent bondsPerm = new Permanent(new BondsOfFaith());
        bondsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(bondsPerm);

        // Verify creature can't attack
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        // Remove Bonds of Faith
        gd.playerBattlefields.get(player2.getId()).remove(bondsPerm);

        // Now creature can attack
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Bonds of Faith")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Bonds of Faith fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bonds of Faith"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bonds of Faith"));
    }
}
