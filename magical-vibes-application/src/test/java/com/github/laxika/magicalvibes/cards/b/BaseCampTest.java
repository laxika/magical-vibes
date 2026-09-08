package com.github.laxika.magicalvibes.cards.b;

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

class BaseCampTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private static Card createCreatureWithRedAbility(String name, CardSubtype subtype) {
        Card card = createCreature(name, "{2}", CardColor.RED, subtype);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(3)), "{R}: You gain 3 life."));
        return card;
    }

    @Test
    @DisplayName("Base Camp enters tapped and adds colorless mana")
    void entersTappedAndAddsColorlessMana() {
        harness.setHand(player1, List.of(new BaseCamp()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Base Camp");

        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana is limited to the four party subtypes")
    void restrictedManaSupportsPartySubtypes() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BaseCamp());
        land.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(
                Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD),
                ManaColor.GREEN)).isEqualTo(1);

        harness.setHand(player1, List.of(createCreature("Test Wizard", "{G}", CardColor.GREEN, CardSubtype.WIZARD)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD))).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot cast a non-party creature")
    void restrictedManaCannotCastNonPartyCreature() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(
                Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD),
                ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(createCreature("Test Elf", "{G}", CardColor.GREEN, CardSubtype.ELF)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can pay for a party creature's activated ability")
    void restrictedManaCanPayPartyAbility() {
        harness.addToBattlefield(player1, createCreatureWithRedAbility("Ability Rogue", CardSubtype.ROGUE));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(
                Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD),
                ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(pool.getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD))).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a non-party ability")
    void restrictedManaCannotPayNonPartyAbility() {
        harness.addToBattlefield(player1, createCreatureWithRedAbility("Ability Elf", CardSubtype.ELF));
        gd.playerManaPools.get(player1.getId()).addSubtypeSpellOrAbilityMana(
                Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD),
                ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
