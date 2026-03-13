package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConvincingMirageTest extends BaseCardTest {

    @Test
    @DisplayName("Convincing Mirage has correct card properties")
    void hasCorrectProperties() {
        ConvincingMirage card = new ConvincingMirage();

        assertThat(card.isAura()).isTrue();
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseBasicLandTypeOnEnterEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(EnchantedPermanentBecomesChosenTypeEffect.class);
    }

    @Test
    @DisplayName("Casting Convincing Mirage puts it on the stack targeting a land")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Convincing Mirage");
        assertThat(entry.getTargetPermanentId()).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Resolving Convincing Mirage attaches it and awaits basic land type choice")
    void resolvingTriggersBasicLandTypeChoice() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Convincing Mirage")
                        && forest.getId().equals(p.getAttachedTo()));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a basic land type sets chosenSubtype on the permanent")
    void choosingTypeSetsOnPermanent() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "ISLAND");

        Permanent mirage = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Convincing Mirage"))
                .findFirst().orElseThrow();
        assertThat(mirage.getChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Enchanted Forest produces blue mana when Island is chosen")
    void enchantedForestProducesChosenMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.ISLAND);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted Mountain produces black mana when Swamp is chosen")
    void enchantedMountainProducesBlackWhenSwampChosen() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(mountain.getId());
        aura.setChosenSubtype(CardSubtype.SWAMP);
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
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(firstForest.getId());
        aura.setChosenSubtype(CardSubtype.ISLAND);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Tap second (non-enchanted) Forest
        gs.tapPermanent(gd, player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted land's subtypes are overridden to chosen type only")
    void enchantedLandSubtypesOverriddenToChosenType() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.PLAINS);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);

        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Normal mana production resumes when Convincing Mirage leaves battlefield")
    void normalManaResumesWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.ISLAND);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast Convincing Mirage targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
