package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MikeyMonaMutantSitters.class, Forest.class, GrizzlyBears.class})
class MikeyMonaMutantSittersTest extends BaseCardTest {

    private static final String COUNTER_MODE =
            "Target player chooses a creature they control and puts two +1/+1 counters on it.";
    private static final String RETURN_MODE =
            "Target player returns a creature or land card from their graveyard to their hand.";

    @Test
    void bothModesTargetDifferentPlayersAndResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card returnedLand = new Forest();
        Card ineligibleCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(returnedLand, ineligibleCard));

        castMikeyAndMona();
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).contains(returnedLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ineligibleCard);
    }

    @Test
    void cannotTargetTheSamePlayerForBothModes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));

        castMikeyAndMona();
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMikeyAndMona() {
        harness.setHand(player1, List.of(new MikeyMonaMutantSitters()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
