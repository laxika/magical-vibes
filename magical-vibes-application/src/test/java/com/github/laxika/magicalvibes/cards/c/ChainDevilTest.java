package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("Chain Devil")
@CardUsed({ChainDevil.class, GrizzlyBears.class})
class ChainDevilTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player sacrifice a nontoken creature")
    void eachPlayerSacrificesNontokenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castChainDevil();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chain Devil");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not sacrifice a creature token")
    void doesNotSacrificeCreatureToken() {
        Permanent token = harness.addToBattlefieldAndReturn(player2, createTokenCreature());

        castChainDevil();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chain Devil");
        harness.assertOnBattlefield(player2, "Zombie Token");
        org.assertj.core.api.Assertions.assertThat(
                harness.getGameData().playerBattlefields.get(player2.getId())).contains(token);
    }

    private void castChainDevil() {
        harness.setHand(player1, List.of(new ChainDevil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Zombie Token");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }
}
