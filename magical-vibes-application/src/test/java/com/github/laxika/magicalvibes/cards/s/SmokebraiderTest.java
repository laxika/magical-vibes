package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HellsparkElemental;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokebraiderTest extends BaseCardTest {

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

    /** A creature with a {R}: gain 3 life activated ability, tagged with the given subtypes. */
    private static Card createCreatureWithRedAbility(String name, CardSubtype... subtypes) {
        Card card = createCreature(name, "{2}", CardColor.RED, subtypes);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(3)), "{R}: You gain 3 life."));
        return card;
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping Smokebraider adds two mana in any combination of colors (spell/ability restricted)")
    void tappingAddsTwoRestrictedManaAnyCombination() {
        Permanent smokebraider = harness.addToBattlefieldAndReturn(player1, new Smokebraider());
        smokebraider.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        assertThat(smokebraider.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        // Each of the two mana gets an independent color choice
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // Nothing lands in the regular pool
        assertThat(pool.get(ManaColor.RED)).isEqualTo(0);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== Casting Elemental spells =====

    @Test
    @DisplayName("Smokebraider mana can cast an Elemental spell")
    void manaCanCastElementalSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.COLORLESS, 2);

        Card elemental = createCreature("Test Elemental", "{2}{R}", CardColor.RED, CardSubtype.ELEMENTAL);
        harness.setHand(player1, List.of(elemental));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Elemental");
    }

    @Test
    @DisplayName("Smokebraider mana cannot cast a non-Elemental spell")
    void manaCannotCastNonElementalSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.GREEN, 1);

        Card elf = createCreature("Test Elf", "{G}", CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(elf));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Smokebraider mana supplements regular mana for an Elemental spell")
    void restrictedManaSupplementsRegularMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.RED, 1);
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.COLORLESS, 1);

        Card elemental = createCreature("Test Elemental", "{1}{R}", CardColor.RED, CardSubtype.ELEMENTAL);
        harness.setHand(player1, List.of(elemental));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Elemental");
    }

    @Test
    @DisplayName("Smokebraider mana can cast a real Elemental card")
    void manaCanCastRealElementalCard() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new HellsparkElemental()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.RED)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.COLORLESS)).isZero();
    }

    // ===== Activating abilities of Elementals =====

    @Test
    @DisplayName("Smokebraider mana can pay for an activated ability of an Elemental")
    void manaCanPayElementalAbility() {
        Card elemental = createCreatureWithRedAbility("Ability Elemental", CardSubtype.ELEMENTAL);
        harness.addToBattlefield(player1, elemental);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        // The restricted mana was consumed
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Smokebraider mana cannot pay for an activated ability of a non-Elemental")
    void manaCannotPayNonElementalAbility() {
        Card goblin = createCreatureWithRedAbility("Ability Goblin", CardSubtype.GOBLIN);
        harness.addToBattlefield(player1, goblin);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Drain behavior =====

    @Test
    @DisplayName("Smokebraider mana drains at step/phase transitions")
    void restrictedManaDrains() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 2);

        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.RED)).isEqualTo(2);

        pool.drainNonPersistent();

        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.RED)).isEqualTo(0);
    }
}
