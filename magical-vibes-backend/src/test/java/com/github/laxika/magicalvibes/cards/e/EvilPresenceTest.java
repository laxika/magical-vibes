package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvilPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Evil Presence has correct card properties")
    void hasCorrectProperties() {
        EvilPresence card = new EvilPresence();

        assertThat(card.isAura()).isTrue();
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(EnchantedPermanentBecomesTypeEffect.class);
        EnchantedPermanentBecomesTypeEffect effect =
                (EnchantedPermanentBecomesTypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Casting Evil Presence puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new EvilPresence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, forest.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Evil Presence");
        assertThat(entry.getTargetPermanentId()).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Resolving Evil Presence attaches it to target land")
    void resolvingAttachesToTargetLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new EvilPresence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Evil Presence")
                        && forest.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted Forest produces black mana instead of green")
    void enchantedForestProducesBlackMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new EvilPresence());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted Mountain produces black mana instead of red")
    void enchantedMountainProducesBlackMana() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new EvilPresence());
        aura.setAttachedTo(mountain.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-enchanted land still produces its normal mana")
    void nonEnchantedLandProducesNormalMana() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent firstForest = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent aura = new Permanent(new EvilPresence());
        aura.setAttachedTo(firstForest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Tap second (non-enchanted) Forest
        gs.tapPermanent(gd, player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted land's subtypes are overridden to Swamp only")
    void enchantedLandSubtypesOverriddenToSwamp() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new EvilPresence());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);

        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Normal mana production resumes when Evil Presence leaves battlefield")
    void normalManaResumesWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new EvilPresence());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Evil Presence on a Swamp still produces black mana")
    void enchantedSwampStillProducesBlackMana() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new EvilPresence());
        aura.setAttachedTo(swamp.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast Evil Presence targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new EvilPresence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
