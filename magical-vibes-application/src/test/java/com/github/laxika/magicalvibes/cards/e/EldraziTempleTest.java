package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EldraziTempleTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        addReadyTemple();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds two colorless Eldrazi-restricted mana")
    void secondAbilityAddsRestrictedMana() {
        addReadyTemple();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana can cast a colorless Eldrazi spell")
    void restrictedManaCanCastColorlessEldraziSpell() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(colorlessEldrazi("Colorless Eldrazi", "{2}")));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot cast a colored Eldrazi spell")
    void restrictedManaCannotCastColoredEldraziSpell() {
        addReadyTemple();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(coloredEldrazi("Colored Eldrazi", "{2}{G}")));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana can pay for a colorless Eldrazi ability")
    void restrictedManaCanPayColorlessEldraziAbility() {
        addReadyTemple();
        Card eldrazi = colorlessEldrazi("Ability Eldrazi", "{2}");
        eldrazi.addActivatedAbility(new ActivatedAbility(
                false, "{1}", List.of(new GainLifeEffect(1)), "{1}: You gain 1 life."));
        harness.addToBattlefield(player1, eldrazi);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getColorlessSubtypeSpellOrAbilityMana(CardSubtype.ELDRAZI)).isEqualTo(1);
    }

    private void addReadyTemple() {
        harness.addToBattlefield(player1, new EldraziTemple());
        findPermanent(player1, "Eldrazi Temple").setSummoningSick(false);
    }

    private static Card colorlessEldrazi(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.ELDRAZI));
        return card;
    }

    private static Card coloredEldrazi(String name, String manaCost) {
        Card card = colorlessEldrazi(name, manaCost);
        card.setColor(CardColor.GREEN);
        return card;
    }
}
