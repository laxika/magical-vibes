package com.github.laxika.magicalvibes.cards.s;

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

class SecludedCourtyardTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type when Secluded Courtyard enters stores that type")
    void choosingCreatureTypeWhenEntering() {
        harness.setHand(player1, List.of(new SecludedCourtyard()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "VAMPIRE");

        assertThat(findPermanent(player1, "Secluded Courtyard").getChosenSubtype())
                .isEqualTo(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        Permanent courtyard = addCourtyard(CardSubtype.VAMPIRE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(courtyard.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana restricted to creature spells and creature-source abilities")
    void secondAbilityAddsRestrictedMana() {
        addCourtyard(CardSubtype.MERFOLK);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(
                Set.of(CardSubtype.MERFOLK), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a creature spell of the chosen type")
    void restrictedManaCanCastChosenCreatureSpell() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.RED);

        Card vampire = createCard("Test Vampire", CardType.CREATURE, "{R}", CardColor.RED,
                CardSubtype.VAMPIRE);
        harness.setHand(player1, List.of(vampire));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a noncreature spell even if it has the chosen subtype")
    void restrictedManaCannotCastNoncreatureSpell() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.RED);

        Card spell = createCard("Test Vampire Spell", CardType.INSTANT, "{R}", CardColor.RED,
                CardSubtype.VAMPIRE);
        harness.setHand(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a creature spell of a different type")
    void restrictedManaCannotCastDifferentCreatureType() {
        addCourtyardAndProduceMana(CardSubtype.VAMPIRE, ManaColor.GREEN);

        Card elf = createCard("Test Elf", CardType.CREATURE, "{G}", CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(elf));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can activate an ability of a creature source of the chosen type")
    void restrictedManaCanActivateChosenCreatureSourceAbility() {
        addCourtyardAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1,
                createAbilitySource("Ability Elemental", CardType.CREATURE, CardSubtype.ELEMENTAL));

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Restricted mana cannot activate an ability of a noncreature source")
    void restrictedManaCannotActivateNoncreatureSourceAbility() {
        addCourtyardAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1,
                createAbilitySource("Noncreature Elemental", CardType.ARTIFACT, CardSubtype.ELEMENTAL));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCourtyard(CardSubtype chosenSubtype) {
        Permanent courtyard = harness.addToBattlefieldAndReturn(player1, new SecludedCourtyard());
        courtyard.setChosenSubtype(chosenSubtype);
        return courtyard;
    }

    private void addCourtyardAndProduceMana(CardSubtype chosenSubtype, ManaColor color) {
        addCourtyard(chosenSubtype);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());
    }

    private static Card createCard(String name, CardType type, String manaCost, CardColor color,
                                   CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setSubtypes(List.of(subtype));
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return card;
    }

    private static Card createAbilitySource(String name, CardType type, CardSubtype subtype) {
        Card card = createCard(name, type, "{2}", CardColor.RED, subtype);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(3)), "{R}: You gain 3 life."));
        return card;
    }
}
