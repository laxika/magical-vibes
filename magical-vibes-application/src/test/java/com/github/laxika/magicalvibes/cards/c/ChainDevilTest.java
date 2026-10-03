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

    @Test
    @DisplayName("Each player chooses one creature and sacrifices wait for both choices")
    void choicesAreCollectedBeforeSacrificing() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ChainDevil());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ChainDevil());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new ChainDevil());

        castChainDevil();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(own.getId()));
        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen);

        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(own).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(remaining);
        harness.assertInGraveyard(player1, "Chain Devil");
        harness.assertInGraveyard(player2, "Chain Devil");
    }

    @Test
    @DisplayName("A creature token is ignored when a nontoken creature is available")
    void sacrificesNontokenCreatureOnMixedBoard() {
        Permanent token = harness.addToBattlefieldAndReturn(player2, createTokenCreature());
        harness.addToBattlefield(player2, new ChainDevil());

        castChainDevil();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chain Devil");
        harness.assertInGraveyard(player2, "Chain Devil");
        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(token);
    }

    @Test
    @DisplayName("The controller still sacrifices when the opponent has no creatures")
    void opponentWithNoCreaturesDoesNotPreventSacrifice() {
        castChainDevil();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chain Devil");
        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
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
