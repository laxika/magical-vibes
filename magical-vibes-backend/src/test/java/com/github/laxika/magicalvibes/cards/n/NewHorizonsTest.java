package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NewHorizonsTest extends BaseCardTest {

    @Test
    @DisplayName("New Horizons has correct card properties")
    void hasCorrectProperties() {
        NewHorizons card = new NewHorizons();

        assertThat(card.isAura()).isTrue();
        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantActivatedAbilityEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("Casting New Horizons puts it on the stack with correct targets")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, List.of(forest.getId(), bears.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("New Horizons");
        assertThat(entry.getTargetId()).isEqualTo(forest.getId());
        assertThat(entry.getTargetIds()).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Resolving New Horizons attaches to land and puts +1/+1 counter on creature")
    void resolvingAttachesAndPutsCounter() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, List.of(forest.getId(), bears.getId()));
        // Resolve the aura spell — aura attaches to land, ETB trigger goes on stack
        harness.passBothPriorities();
        // Resolve the ETB trigger — put +1/+1 counter on creature
        harness.passBothPriorities();

        // Aura is attached to forest
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("New Horizons")
                        && forest.getId().equals(p.getAttachedTo()));
        // Creature has +1/+1 counter
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted land gains mana ability that produces two mana of any one color")
    void enchantedLandGainsManaAbility() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new NewHorizons());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Activate the granted mana ability on the forest (ability index 0)
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        // Forest should be tapped after activating the ability
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land can still produce its normal mana when tapped directly")
    void enchantedLandStillProducesNormalMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new NewHorizons());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Tap the forest directly for normal mana (not using the granted ability)
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted mana ability goes away when aura leaves battlefield")
    void manaAbilityRemovedWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new NewHorizons());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        // Forest should have no granted activated abilities
        var staticBonus = gqs.computeStaticBonus(gd, forest);
        assertThat(staticBonus.grantedActivatedAbilities()).isEmpty();
    }
}
