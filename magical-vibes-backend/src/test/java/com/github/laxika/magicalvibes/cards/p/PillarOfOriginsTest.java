package com.github.laxika.magicalvibes.cards.p;

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
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PillarOfOriginsTest extends BaseCardTest {

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
        PillarOfOrigins card = new PillarOfOrigins();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseSubtypeOnEnterEffect.class);
    }

    @Test
    @DisplayName("Has activated ability with tap and AwardAnyColorChosenSubtypeCreatureManaEffect")
    void hasActivatedAbility() {
        PillarOfOrigins card = new PillarOfOrigins();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(1)
                .first()
                .isInstanceOf(AwardAnyColorChosenSubtypeCreatureManaEffect.class);
    }

    // ===== Entering the battlefield =====

    @Test
    @DisplayName("Casting and resolving Pillar prompts for creature type choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new PillarOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pillar of Origins"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
    }

    @Test
    @DisplayName("Choosing a creature type sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new PillarOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "VAMPIRE");

        Permanent pillar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pillar of Origins"))
                .findFirst().orElseThrow();
        assertThat(pillar.getChosenSubtype()).isEqualTo(CardSubtype.VAMPIRE);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping Pillar prompts for mana color choice")
    void tappingPromptsForColorChoice() {
        harness.addToBattlefield(player1, new PillarOfOrigins());
        Permanent pillar = gd.playerBattlefields.get(player1.getId()).getFirst();
        pillar.setChosenSubtype(CardSubtype.DINOSAUR);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pillar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
    }

    @Test
    @DisplayName("Choosing a color adds mana to the subtype creature mana pool")
    void choosingColorAddsRestrictedMana() {
        harness.addToBattlefield(player1, new PillarOfOrigins());
        Permanent pillar = gd.playerBattlefields.get(player1.getId()).getFirst();
        pillar.setChosenSubtype(CardSubtype.MERFOLK);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // Regular blue mana should NOT have increased
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        // Subtype creature mana for MERFOLK should have 1 blue
        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.MERFOLK), ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== Mana restriction: can cast creature of chosen type =====

    @Test
    @DisplayName("Mana from Pillar can be used to cast a creature spell of the chosen type")
    void manaCanCastCreatureOfChosenType() {
        // Set up Pillar with VAMPIRE chosen
        harness.addToBattlefield(player1, new PillarOfOrigins());
        Permanent pillar = gd.playerBattlefields.get(player1.getId()).getFirst();
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);

        // Add subtype creature mana (simulate tapping Pillar and choosing white)
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.WHITE, 1);
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.COLORLESS, 2);

        // Create a Vampire creature that costs {2}{W}
        Card vampire = createCreature("Test Vampire", "{2}{W}", CardColor.WHITE, CardSubtype.VAMPIRE);
        harness.setHand(player1, List.of(vampire));

        // Should be able to cast it using only the restricted mana
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Vampire");
    }

    // ===== Mana restriction: cannot cast creature of different type =====

    @Test
    @DisplayName("Mana from Pillar cannot be used to cast a creature spell of a different type")
    void manaCannotCastCreatureOfDifferentType() {
        harness.addToBattlefield(player1, new PillarOfOrigins());
        Permanent pillar = gd.playerBattlefields.get(player1.getId()).getFirst();
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);

        // Add subtype creature mana (restricted to Vampire)
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.GREEN, 1);

        // Create a non-Vampire creature that costs {G}
        Card elf = createCreature("Test Elf", "{G}", CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(elf));

        // Should NOT be able to cast — the only green mana available is restricted to Vampires
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mana restriction: cannot cast non-creature spell =====

    @Test
    @DisplayName("Mana from Pillar cannot be used to cast a non-creature spell")
    void manaCannotCastNonCreatureSpell() {
        harness.addToBattlefield(player1, new PillarOfOrigins());
        Permanent pillar = gd.playerBattlefields.get(player1.getId()).getFirst();
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);

        // Add subtype creature mana (restricted to Vampire creatures)
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.RED, 1);

        // Create an instant that costs {R}
        Card instant = new Card();
        instant.setName("Test Bolt");
        instant.setType(CardType.INSTANT);
        instant.setManaCost("{R}");
        instant.setColor(CardColor.RED);
        harness.setHand(player1, List.of(instant));

        // Should NOT be able to cast — it's not a creature spell
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mana from Pillar works alongside regular mana =====

    @Test
    @DisplayName("Restricted mana supplements regular mana for casting creature of chosen type")
    void restrictedManaSupplementsRegularMana() {
        harness.addToBattlefield(player1, new PillarOfOrigins());
        Permanent pillar = gd.playerBattlefields.get(player1.getId()).getFirst();
        pillar.setChosenSubtype(CardSubtype.DINOSAUR);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // 1 regular green + 1 subtype-restricted colorless
        pool.add(ManaColor.GREEN, 1);
        pool.addSubtypeCreatureMana(CardSubtype.DINOSAUR, ManaColor.COLORLESS, 1);

        // Create a Dinosaur that costs {1}{G}
        Card dino = createCreature("Test Dinosaur", "{1}{G}", CardColor.GREEN, CardSubtype.DINOSAUR);
        harness.setHand(player1, List.of(dino));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Dinosaur");
    }

    // ===== Mana drains at step transitions =====

    @Test
    @DisplayName("Subtype creature mana drains at step/phase transitions")
    void subtypeManaHasDrainBehavior() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.WHITE, 2);

        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.VAMPIRE), ManaColor.WHITE)).isEqualTo(2);

        pool.drainNonPersistent();

        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.VAMPIRE), ManaColor.WHITE)).isEqualTo(0);
    }
}
