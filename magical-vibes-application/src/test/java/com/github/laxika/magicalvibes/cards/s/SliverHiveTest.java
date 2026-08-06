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

class SliverHiveTest extends BaseCardTest {

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

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tappingForColorless() {
        Permanent hive = harness.addToBattlefieldAndReturn(player1, new SliverHive());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds one mana of a chosen color usable only for Sliver spells")
    void tappingForRestrictedAnyColorMana() {
        Permanent hive = harness.addToBattlefieldAndReturn(player1, new SliverHive());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(hive.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.SLIVER), ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a Sliver spell")
    void restrictedManaCastsSliverSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.SLIVER, ManaColor.GREEN, 1);
        pool.addSubtypeCreatureMana(CardSubtype.SLIVER, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(createCreature("Test Sliver", "{1}{G}", CardColor.GREEN, CardSubtype.SLIVER)));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getSubtypeCreatureManaTotal(Set.of(CardSubtype.SLIVER))).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot cast a non-Sliver spell")
    void restrictedManaCannotCastNonSliverSpell() {
        gd.playerManaPools.get(player1.getId()).addSubtypeCreatureMana(CardSubtype.SLIVER, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(createCreature("Test Elf", "{G}", CardColor.GREEN, CardSubtype.ELF)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for an activated ability of a Sliver")
    void restrictedManaCannotPayAbilityOfSliver() {
        harness.addToBattlefield(player1, createCreatureWithRedAbility("Ability Sliver", CardSubtype.SLIVER));
        gd.playerManaPools.get(player1.getId()).addSubtypeCreatureMana(CardSubtype.SLIVER, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Token ability cannot be activated without a Sliver on the battlefield")
    void tokenAbilityRequiresASliver() {
        harness.addToBattlefieldAndReturn(player1, new SliverHive());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Token ability creates a 1/1 Sliver when you control a Sliver")
    void tokenAbilityCreatesSliverToken() {
        harness.addToBattlefieldAndReturn(player1, new SliverHive());
        harness.addToBattlefield(player1, createCreature("Test Sliver", "{G}", CardColor.GREEN, CardSubtype.SLIVER));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SLIVER);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }
}
