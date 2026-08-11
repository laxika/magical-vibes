package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlamebraiderTest extends BaseCardTest {

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

    @Test
    @DisplayName("Tapping Flamebraider adds two independently chosen Elemental-restricted mana")
    void tappingAddsTwoRestrictedMana() {
        Permanent flamebraider = harness.addToBattlefieldAndReturn(player1, new Flamebraider());
        flamebraider.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(flamebraider.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flamebraider mana can pay for an Elemental spell")
    void manaCanCastElementalSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        harness.setHand(player1, List.of(createCreature("Test Elemental", "{R}", CardColor.RED, CardSubtype.ELEMENTAL)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Flamebraider mana cannot pay for a non-Elemental spell")
    void manaCannotCastNonElementalSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        harness.setHand(player1, List.of(createCreature("Test Goblin", "{R}", CardColor.RED, CardSubtype.GOBLIN)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
