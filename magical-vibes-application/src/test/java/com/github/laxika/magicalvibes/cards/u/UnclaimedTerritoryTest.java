package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnclaimedTerritoryTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has ChooseSubtypeOnEnterEffect on ON_ENTER_BATTLEFIELD")
    void hasChooseSubtypeEffect() {
        UnclaimedTerritory card = new UnclaimedTerritory();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseSubtypeOnEnterEffect.class);
    }

    @Test
    @DisplayName("Has two activated abilities: colorless mana and restricted any-color mana")
    void hasTwoActivatedAbilities() {
        UnclaimedTerritory card = new UnclaimedTerritory();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {T}: Add {C}
        var colorlessAbility = card.getActivatedAbilities().get(0);
        assertThat(colorlessAbility.isRequiresTap()).isTrue();
        assertThat(colorlessAbility.getManaCost()).isNull();
        assertThat(colorlessAbility.getEffects()).hasSize(1);
        assertThat(colorlessAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) colorlessAbility.getEffects().getFirst()).color()).isEqualTo(ManaColor.COLORLESS);

        // Second ability: {T}: Add one mana of any color (restricted)
        var restrictedAbility = card.getActivatedAbilities().get(1);
        assertThat(restrictedAbility.isRequiresTap()).isTrue();
        assertThat(restrictedAbility.getManaCost()).isNull();
        assertThat(restrictedAbility.getEffects()).hasSize(1);
        assertThat(restrictedAbility.getEffects().getFirst())
                .isInstanceOf(AwardAnyColorChosenSubtypeCreatureManaEffect.class);
    }

    // ===== Colorless mana ability =====

    @Test
    @DisplayName("Tapping with first ability adds colorless mana")
    void tappingForColorlessMana() {
        harness.addToBattlefield(player1, new UnclaimedTerritory());
        Permanent territory = gd.playerBattlefields.get(player1.getId()).getFirst();
        territory.setChosenSubtype(CardSubtype.PIRATE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(territory.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    // ===== Restricted mana ability =====

    @Test
    @DisplayName("Tapping with second ability prompts for mana color choice")
    void tappingForRestrictedManaPromptsColorChoice() {
        harness.addToBattlefield(player1, new UnclaimedTerritory());
        Permanent territory = gd.playerBattlefields.get(player1.getId()).getFirst();
        territory.setChosenSubtype(CardSubtype.DINOSAUR);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(territory.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
    }

    @Test
    @DisplayName("Choosing a color adds mana to the subtype creature mana pool")
    void choosingColorAddsRestrictedMana() {
        harness.addToBattlefield(player1, new UnclaimedTerritory());
        Permanent territory = gd.playerBattlefields.get(player1.getId()).getFirst();
        territory.setChosenSubtype(CardSubtype.MERFOLK);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // Regular blue mana should NOT have increased
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        // Subtype creature mana for MERFOLK should have 1 blue
        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.MERFOLK), ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== Mana restriction: can cast creature of chosen type =====

    @Test
    @DisplayName("Mana from second ability can be used to cast a creature spell of the chosen type")
    void manaCanCastCreatureOfChosenType() {
        harness.addToBattlefield(player1, new UnclaimedTerritory());
        Permanent territory = gd.playerBattlefields.get(player1.getId()).getFirst();
        territory.setChosenSubtype(CardSubtype.VAMPIRE);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.WHITE, 1);
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.COLORLESS, 2);

        Card vampire = createCreature("Test Vampire", "{2}{W}", CardColor.WHITE, CardSubtype.VAMPIRE);
        harness.setHand(player1, List.of(vampire));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Vampire");
    }

    // ===== Mana restriction: cannot cast creature of different type =====

    @Test
    @DisplayName("Mana from second ability cannot be used to cast a creature spell of a different type")
    void manaCannotCastCreatureOfDifferentType() {
        harness.addToBattlefield(player1, new UnclaimedTerritory());
        Permanent territory = gd.playerBattlefields.get(player1.getId()).getFirst();
        territory.setChosenSubtype(CardSubtype.VAMPIRE);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.GREEN, 1);

        Card elf = createCreature("Test Elf", "{G}", CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(elf));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mana restriction: cannot cast non-creature spell =====

    @Test
    @DisplayName("Mana from second ability cannot be used to cast a non-creature spell")
    void manaCannotCastNonCreatureSpell() {
        harness.addToBattlefield(player1, new UnclaimedTerritory());
        Permanent territory = gd.playerBattlefields.get(player1.getId()).getFirst();
        territory.setChosenSubtype(CardSubtype.VAMPIRE);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.RED, 1);

        Card instant = new Card();
        instant.setName("Test Bolt");
        instant.setType(CardType.INSTANT);
        instant.setManaCost("{R}");
        instant.setColor(CardColor.RED);
        harness.setHand(player1, List.of(instant));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mixed mana usage =====

    @Test
    @DisplayName("Restricted mana supplements regular mana for casting creature of chosen type")
    void restrictedManaSupplementsRegularMana() {
        harness.addToBattlefield(player1, new UnclaimedTerritory());
        Permanent territory = gd.playerBattlefields.get(player1.getId()).getFirst();
        territory.setChosenSubtype(CardSubtype.DINOSAUR);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.addSubtypeCreatureMana(CardSubtype.DINOSAUR, ManaColor.COLORLESS, 1);

        Card dino = createCreature("Test Dinosaur", "{1}{G}", CardColor.GREEN, CardSubtype.DINOSAUR);
        harness.setHand(player1, List.of(dino));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Dinosaur");
    }
}
