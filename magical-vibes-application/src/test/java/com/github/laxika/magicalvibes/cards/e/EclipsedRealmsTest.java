package com.github.laxika.magicalvibes.cards.e;

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

class EclipsedRealmsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a subtype offers only the eight types named by Eclipsed Realms")
    void subtypeChoiceIsRestricted() {
        harness.setHand(player1, List.of(new EclipsedRealms()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly(
                "ELEMENTAL", "ELF", "FAERIE", "GIANT", "GOBLIN", "KITHKIN", "MERFOLK", "TREEFOLK");
    }

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        Permanent land = addLand(CardSubtype.ELF);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana restricted to the chosen type's spells and abilities")
    void secondAbilityAddsChosenTypeRestrictedMana() {
        addLand(CardSubtype.FAERIE);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.FAERIE), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a spell of the chosen type")
    void restrictedManaCanCastChosenTypeSpell() {
        addLandAndProduceMana(CardSubtype.FAERIE, ManaColor.RED);

        Card faerieSpell = createSpell("Test Faerie Spell", CardSubtype.FAERIE);
        harness.setHand(player1, List.of(faerieSpell));

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a spell of another type")
    void restrictedManaCannotCastAnotherTypeSpell() {
        addLandAndProduceMana(CardSubtype.FAERIE, ManaColor.RED);

        Card elfSpell = createSpell("Test Elf Spell", CardSubtype.ELF);
        harness.setHand(player1, List.of(elfSpell));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can activate an ability of a source of the chosen type")
    void restrictedManaCanActivateChosenTypeAbility() {
        addLandAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1,
                createCreatureWithLifeAbility("Ability Elemental", CardSubtype.ELEMENTAL));

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Restricted mana cannot activate an ability of a source of another type")
    void restrictedManaCannotActivateAnotherTypeAbility() {
        addLandAndProduceMana(CardSubtype.ELEMENTAL, ManaColor.RED);
        harness.addToBattlefield(player1, createCreatureWithLifeAbility("Ability Elf", CardSubtype.ELF));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLand(CardSubtype chosenSubtype) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EclipsedRealms());
        land.setChosenSubtype(chosenSubtype);
        return land;
    }

    private void addLandAndProduceMana(CardSubtype chosenSubtype, ManaColor color) {
        addLand(chosenSubtype);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());
    }

    private static Card createSpell(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private static Card createCreatureWithLifeAbility(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.RED);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(3)), "{R}: You gain 3 life."));
        return card;
    }
}
