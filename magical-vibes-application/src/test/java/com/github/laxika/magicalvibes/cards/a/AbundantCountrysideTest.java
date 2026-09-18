package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AbundantCountryside.class)
class AbundantCountrysideTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new AbundantCountryside());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a creature spell")
    void restrictedManaCastsCreatureSpell() {
        harness.addToBattlefield(player1, new AbundantCountryside());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);

        harness.setHand(player1, List.of(createCreature("Test Beast", "{G}", CardColor.GREEN)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getCreatureSpellOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Six mana creates a colorless Shapeshifter token with changeling")
    void createsShapeshifterToken() {
        harness.addToBattlefield(player1, new AbundantCountryside());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
    }

    private static Card createCreature(String name, String manaCost, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
