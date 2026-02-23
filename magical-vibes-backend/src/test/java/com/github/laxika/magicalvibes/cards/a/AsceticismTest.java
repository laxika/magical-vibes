package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsceticismTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Asceticism has correct card properties")
    void hasCorrectProperties() {
        Asceticism card = new Asceticism();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantEffect.class);
        GrantEffect grantEffect = (GrantEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(grantEffect.effect()).isInstanceOf(CantBeTargetOfSpellsOrAbilitiesEffect.class);
        assertThat(grantEffect.scope()).isEqualTo(GrantKeywordEffect.Scope.OWN_CREATURES);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Asceticism puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Asceticism()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Asceticism");
    }

    // ===== CantBeTargetOfSpellsOrAbilities: own creatures gain effect =====

    @Test
    @DisplayName("Creatures you control gain can't-be-targeted effect")
    void ownCreaturesGainCantBeTargeted() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Asceticism());

        assertThat(gqs.hasGrantedEffect(gd, bears, CantBeTargetOfSpellsOrAbilitiesEffect.class)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain can't-be-targeted effect")
    void opponentCreaturesDoNotGainEffect() {
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Asceticism());

        assertThat(gqs.hasGrantedEffect(gd, opponentBears, CantBeTargetOfSpellsOrAbilitiesEffect.class)).isFalse();
    }

    @Test
    @DisplayName("Can't-be-targeted effect is removed when Asceticism leaves the battlefield")
    void effectRemovedWhenSourceLeaves() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Asceticism());
        assertThat(gqs.hasGrantedEffect(gd, bears, CantBeTargetOfSpellsOrAbilitiesEffect.class)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Asceticism"));

        assertThat(gqs.hasGrantedEffect(gd, bears, CantBeTargetOfSpellsOrAbilitiesEffect.class)).isFalse();
    }

    // ===== Targeting prevention: opponent cannot target =====

    @Test
    @DisplayName("Opponent cannot target your creature with spells while Asceticism is on battlefield")
    void opponentCannotTargetYourCreature() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Asceticism());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    // ===== Targeting prevention: controller CAN target =====

    @Test
    @DisplayName("Controller can target own creatures despite Asceticism's effect")
    void controllerCanTargetOwnCreatures() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Asceticism());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bears.getId());
    }

    // ===== Regenerate activated ability =====

    @Test
    @DisplayName("Activating regeneration targets a creature and puts ability on stack")
    void activatingRegenTargetsCreature() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Asceticism");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield to target creature")
    void resolvingRegenGrantsShield() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate opponent's creature")
    void canRegenerateOpponentCreature() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateRegenWithoutMana() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target non-creature with regeneration ability")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Asceticism());
        // Target Asceticism itself (an enchantment, not a creature)
        UUID asceticismId = harness.getPermanentId(player1, "Asceticism");
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, asceticismId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability")
    void manaConsumedOnRegenActivation() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration fizzles if target creature is removed before resolution")
    void regenFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, bears.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate regeneration multiple times on the same creature")
    void canStackMultipleRegenerationShields() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(2);
    }

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
