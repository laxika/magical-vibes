package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BruvacTheGrandiloquent.class, Millstone.class, Forest.class, GrizzlyBears.class})
class BruvacTheGrandiloquentTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles cards milled from an opponent's library")
    void doublesOpponentMill() {
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, cards(6));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Does not double cards milled from its controller's library")
    void doesNotDoubleControllerMill() {
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, cards(6));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
