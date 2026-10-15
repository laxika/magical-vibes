package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AcidicSliver;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.v.VenomSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SliverHive.class, VenomSliver.class, ElvishMystic.class, AcidicSliver.class})
class SliverHiveTest extends BaseCardTest {

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
        assertThat(pool.getSubtypeSpellOnlyManaForColor(Set.of(CardSubtype.SLIVER), ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a Sliver spell")
    void restrictedManaCastsSliverSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        harness.addToBattlefield(player1, new SliverHive());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new VenomSliver()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.SLIVER))).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot cast a non-Sliver spell")
    void restrictedManaCannotCastNonSliverSpell() {
        harness.addToBattlefield(player1, new SliverHive());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new ElvishMystic()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(AcidicSliver.class)
    @DisplayName("Restricted mana cannot pay for an activated ability of a Sliver")
    void restrictedManaCannotPayAbilityOfSliver() {
        harness.addToBattlefield(player1, new AcidicSliver());
        harness.addToBattlefield(player1, new SliverHive());
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
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
        Permanent hive = harness.addToBattlefieldAndReturn(player1, new SliverHive());
        harness.addToBattlefield(player1, new VenomSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(hive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SLIVER);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    void opposingSliverDoesNotAllowTokenActivation() {
        Permanent hive = harness.addToBattlefieldAndReturn(player1, new SliverHive());
        harness.addToBattlefield(player2, new VenomSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hive.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenAbilityRequiresFiveMana() {
        Permanent hive = harness.addToBattlefieldAndReturn(player1, new SliverHive());
        harness.addToBattlefield(player1, new VenomSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hive.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenAbilityResolvesAfterLastSliverLeaves() {
        harness.addToBattlefield(player1, new SliverHive());
        harness.addToBattlefield(player1, new AcidicSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 2, null, null);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Acidic Sliver");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }
}
