package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PillarOfTheParuns.class)
class PillarOfTheParunsTest extends BaseCardTest {

    @Test
    void tappingAddsManaRestrictedToMulticoloredSpells() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getMulticoloredSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCastsMulticoloredSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        Card spell = creature("Multicolored Creature", "{U}", CardColor.BLUE, CardColor.RED);
        harness.setHand(player1, List.of(spell));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void restrictedManaCannotCastMonocoloredSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        Card spell = creature("Monocolored Creature", "{U}", CardColor.BLUE);
        harness.setHand(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private static Card creature(String name, String manaCost, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColors(List.of(colors));
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
