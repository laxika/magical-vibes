package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ActionNewsCrew.class, Forest.class, GrizzlyBears.class})
class ActionNewsCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Channel puts a +1/+1 counter on each creature you control and draws a card")
    void channelsCountersAndDraws() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ActionNewsCrew()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Action News Crew");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Vigilance allows Action News Crew to attack without tapping")
    void attacksWithoutTapping() {
        Permanent crew = addCreatureReady(player1, new ActionNewsCrew());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(crew.isAttacking()).isTrue();
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Channel draws a card even when you control no creatures")
    void drawsWithoutCreatures() {
        harness.setHand(player1, List.of(new ActionNewsCrew()));
        harness.setLibrary(player1, List.of(new ActionNewsCrew(), new ActionNewsCrew()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Action News Crew");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Action News Crew");
    }

    @Test
    @DisplayName("Channel includes creatures entering before the ability resolves")
    void countersCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new ActionNewsCrew()));
        harness.setLibrary(player1, List.of(new ActionNewsCrew()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateHandAbility(player1, 0, null);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new ActionNewsCrew());
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Action News Crew");
    }

    @Test
    @DisplayName("Channel requires six mana and does not discard when payment fails")
    void cannotChannelWithInsufficientMana() {
        harness.setHand(player1, List.of(new ActionNewsCrew()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Action News Crew");
        harness.assertNotInGraveyard(player1, "Action News Crew");
        assertThat(gd.stack).isEmpty();
    }
}
