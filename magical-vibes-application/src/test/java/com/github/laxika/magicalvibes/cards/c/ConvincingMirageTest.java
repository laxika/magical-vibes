package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConvincingMirage.class, Forest.class, Mountain.class, RuneclawBear.class, TerramorphicExpanse.class})
class ConvincingMirageTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Convincing Mirage puts it on the stack targeting a land")
    void castingPutsOnStack() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Convincing Mirage");
        assertThat(entry.getTargetId()).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Resolving Convincing Mirage attaches it and awaits basic land type choice")
    void resolvingTriggersBasicLandTypeChoice() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Convincing Mirage")
                        && forest.getId().equals(p.getAttachedTo()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a basic land type sets chosenSubtype on the permanent")
    void choosingTypeSetsOnPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        Permanent mirage = findPermanent(player1, "Convincing Mirage");
        assertThat(mirage.getChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Enchanted Forest produces blue mana when Island is chosen")
    void enchantedForestProducesChosenMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.ISLAND);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted Mountain produces black mana when Swamp is chosen")
    void enchantedMountainProducesBlackWhenSwampChosen() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(mountain.getId());
        aura.setChosenSubtype(CardSubtype.SWAMP);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 0);

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
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted land's subtypes are overridden to chosen type only")
    void enchantedLandSubtypesOverriddenToChosenType() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.PLAINS);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Normal mana production resumes when Convincing Mirage leaves battlefield")
    void normalManaResumesWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = new Permanent(new ConvincingMirage());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.ISLAND);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast Convincing Mirage targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        harness.addToBattlefield(player1, new RuneclawBear());
        Permanent bears = findPermanent(player1, "Runeclaw Bear");
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @ParameterizedTest
    @CsvSource({"PLAINS, WHITE", "ISLAND, BLUE", "SWAMP, BLACK", "MOUNTAIN, RED", "FOREST, GREEN"})
    @DisplayName("The Aura controller chooses any basic land type for an opponent's land")
    void changesOpponentsLand(CardSubtype subtype, ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(subtype);
        assertThat(findPermanent(player1, "Convincing Mirage").getAttachedTo()).isEqualTo(land.getId());
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isEqualTo(1);
        for (ManaColor other : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            if (other != color) {
                assertThat(gd.playerManaPools.get(player2.getId()).get(other)).isZero();
            }
        }
    }

    @Test
    @DisplayName("A nonbasic land gains chosen mana and loses its printed activated ability")
    void nonbasicLandLosesPrintedAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TerramorphicExpanse());
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        harness.activateAbility(player1, 0, null, null);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Terramorphic Expanse");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Aura with a departed target goes to the graveyard without choosing a type")
    void departedTargetPreventsEntryAndChoice() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ConvincingMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(land);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Convincing Mirage");
        harness.assertNotOnBattlefield(player1, "Convincing Mirage");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
