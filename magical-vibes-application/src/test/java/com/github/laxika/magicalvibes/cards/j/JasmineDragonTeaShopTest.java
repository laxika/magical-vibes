package com.github.laxika.magicalvibes.cards.j;

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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(JasmineDragonTeaShop.class)
class JasmineDragonTeaShopTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tappingForColorlessMana() {
        Permanent shop = addReadyShop();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(shop.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds one mana restricted to Ally spells and abilities")
    void tappingForRestrictedAnyColorMana() {
        Permanent shop = addReadyShop();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(shop.isTapped()).isTrue();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ALLY), ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast an Ally but not a non-Ally spell")
    void restrictedManaCanCastOnlyAllySpells() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ALLY, ManaColor.WHITE, 1);

        harness.setHand(player1, List.of(createCreature("Test Elf", "{W}", CardColor.WHITE, CardSubtype.ELF)));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ALLY, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(createCreature("Test Ally", "{W}", CardColor.WHITE, CardSubtype.ALLY)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana can pay for an Ally source ability but not a non-Ally source ability")
    void restrictedManaCanPayOnlyAllySourceAbilities() {
        Card ally = createCreatureWithAbility("Test Ally", CardSubtype.ALLY);
        Card elf = createCreatureWithAbility("Test Elf", CardSubtype.ELF);
        harness.addToBattlefield(player1, ally);
        harness.addToBattlefield(player1, elf);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ALLY, ManaColor.WHITE, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Third ability creates a 1/1 white Ally token")
    void createsAllyToken() {
        Permanent shop = addReadyShop();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Ally");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ALLY);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(shop.isTapped()).isTrue();
    }

    private Permanent addReadyShop() {
        Permanent shop = new Permanent(new JasmineDragonTeaShop());
        shop.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(shop);
        return shop;
    }

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

    private static Card createCreatureWithAbility(String name, CardSubtype subtype) {
        Card card = createCreature(name, "{2}", CardColor.WHITE, subtype);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{W}", List.of(new GainLifeEffect(1)), "{W}: You gain 1 life."));
        return card;
    }
}
