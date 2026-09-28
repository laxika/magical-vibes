package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErisRoarOfTheStorm.class, Shock.class, Divination.class, GrizzlyBears.class})
class ErisRoarOfTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("costs two less for each distinct instant or sorcery mana value in your graveyard")
    void reducesCostForDistinctInstantOrSorceryManaValues() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ErisRoarOfTheStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eris, Roar of the Storm");
    }

    @Test
    @DisplayName("does not reduce its cost for creature cards in your graveyard")
    void ignoresNonInstantOrSorceryCardsForCostReduction() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ErisRoarOfTheStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("creates a flying prowess Dragon Elemental on the second spell each turn")
    void createsDragonElementalOnSecondSpell() {
        harness.addToBattlefield(player1, new ErisRoarOfTheStorm());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Dragon Elemental");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.DRAGON, CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.PROWESS);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
    }
}
