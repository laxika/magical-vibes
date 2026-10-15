package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
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

@CardUsed({MikeyMonaMutantSitters.class, Forest.class, GrizzlyBears.class, SolRing.class})
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
        castMikeyAndMona();
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterModeAloneLetsOpponentChooseTheirCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        castMikeyAndMona();
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
    }

    @Test
    void returnModeAloneReturnsCreatureWithoutPlacingCounters() {
        Card creature = new MikeyMonaMutantSitters();
        harness.setGraveyard(player1, List.of(creature));

        castMikeyAndMona();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void counterModeCanTargetPlayerWithoutCreatures() {
        castMikeyAndMona();
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnModeCanTargetPlayerWithEmptyGraveyard() {
        castMikeyAndMona();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnModeIgnoresNoncreatureNonlandCards() {
        Card artifact = new SolRing();
        Card creature = new MikeyMonaMutantSitters();
        harness.setGraveyard(player2, List.of(artifact, creature));

        castMikeyAndMona();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(creature).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bothModesResolveWhenFirstPlayerHasNoCreatures() {
        Card creature = new MikeyMonaMutantSitters();
        harness.setGraveyard(player1, List.of(creature));

        castMikeyAndMona();
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bothModesResolveInPrintedOrderEvenWhenSelectedInReverse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card returnedCard = new MikeyMonaMutantSitters();
        harness.setGraveyard(player2, List.of(returnedCard));

        castMikeyAndMona();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(returnedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(returnedCard);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).contains(returnedCard);
    }

    private void castMikeyAndMona() {
        harness.setHand(player1, List.of(new MikeyMonaMutantSitters()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
