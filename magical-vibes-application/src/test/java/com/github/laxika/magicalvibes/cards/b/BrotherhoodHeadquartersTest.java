package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BrotherhoodHeadquarters.class)
class BrotherhoodHeadquartersTest extends BaseCardTest {

    @Test
    @DisplayName("Brotherhood Headquarters adds restricted mana of a chosen color")
    void addsRestrictedMana() {
        harness.addToBattlefieldAndReturn(player1, new BrotherhoodHeadquarters());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(
                Set.of(CardSubtype.ASSASSIN_OR_FREERUNNING), ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast Assassin and freerunning spells")
    void restrictedManaCastsAssassinAndFreerunningSpells() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(
                CardSubtype.ASSASSIN_OR_FREERUNNING, ManaColor.BLACK, 1);

        Card freerunningSpell = createSpell("Test Freerunning Spell", Keyword.FREERUNNING);
        harness.setHand(player1, List.of(freerunningSpell));
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        pool.addSubtypeSpellOrAbilityMana(
                CardSubtype.ASSASSIN_OR_FREERUNNING, ManaColor.BLACK, 1);
        Card assassinSpell = createCreature("Test Assassin", CardSubtype.ASSASSIN);
        harness.setHand(player1, List.of(assassinSpell));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for unrelated spells or abilities")
    void restrictedManaRejectsUnrelatedObjects() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(
                CardSubtype.ASSASSIN_OR_FREERUNNING, ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(createCreature("Test Elf", CardSubtype.ELF)));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        pool.addSubtypeSpellOrAbilityMana(
                CardSubtype.ASSASSIN_OR_FREERUNNING, ManaColor.RED, 1);
        harness.addToBattlefield(player1, createCreatureWithAbility("Ability Elf", CardSubtype.ELF));
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can activate an Assassin source ability")
    void restrictedManaPaysAssassinAbility() {
        Card assassin = createCreatureWithAbility("Ability Assassin", CardSubtype.ASSASSIN);
        harness.addToBattlefield(player1, assassin);
        gd.playerManaPools.get(player1.getId()).addSubtypeSpellOrAbilityMana(
                CardSubtype.ASSASSIN_OR_FREERUNNING, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private static Card createSpell(String name, Keyword keyword) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost("{B}");
        card.setColor(CardColor.BLACK);
        card.setKeywords(Set.of(keyword));
        return card;
    }

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private static Card createCreatureWithAbility(String name, CardSubtype subtype) {
        Card card = createCreature(name, subtype);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(3)), "{R}: You gain 3 life."));
        return card;
    }
}
