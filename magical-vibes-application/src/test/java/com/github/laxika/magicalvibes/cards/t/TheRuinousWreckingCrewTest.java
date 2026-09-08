package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheRuinousWreckingCrew.class, GrizzlyBears.class, Plains.class})
class TheRuinousWreckingCrewTest extends BaseCardTest {

    private static final String DISCARD_AND_DRAW = "Discard a card, then draw a card.";
    private static final String LOSE_LIFE = "Target opponent loses 2 life.";
    private static final String DESTROY_TOKEN = "Destroy target token.";
    private static final String SACRIFICE_CREATURE = "Each player sacrifices a creature of their choice.";

    @Test
    void entersWithXCountersAndEachPlayerSacrificesAChosenCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent crew = castForX(1);
        harness.handleListChoice(player1, SACRIFICE_CREATURE);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(crew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    void discardModeDiscardsThenDraws() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Plains();
        harness.setHand(player1, List.of(new TheRuinousWreckingCrew(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        addManaForX(1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_AND_DRAW);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void canChooseUpToXTargetedModes() {
        Permanent token = addToken(player2);
        Permanent crew = castForX(2);

        harness.handleListChoice(player1, LOSE_LIFE);
        harness.handleListChoice(player1, DESTROY_TOKEN);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(crew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    void destroyTokenModeRejectsNonTokenPermanents() {
        Permanent nonToken = harness.addToBattlefieldAndReturn(player2, new Plains());
        addToken(player2);
        castForX(1);
        harness.handleListChoice(player1, DESTROY_TOKEN);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonToken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castForX(int xValue) {
        harness.setHand(player1, List.of(new TheRuinousWreckingCrew()));
        addManaForX(xValue);
        harness.castCreature(player1, 0, xValue);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player owner) {
        Card token = new Card();
        token.setName("Token");
        token.setType(CardType.ARTIFACT);
        token.setManaCost("");
        token.setToken(true);
        return harness.addToBattlefieldAndReturn(owner, token);
    }
}
