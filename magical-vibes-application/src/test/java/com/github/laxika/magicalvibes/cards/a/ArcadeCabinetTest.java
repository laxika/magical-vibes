package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcadeCabinet.class, GrizzlyBears.class})
class ArcadeCabinetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter on each of up to four target creatures")
    void putsCountersOnUpToFourTargetCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());
        Permanent fourth = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArcadeCabinet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fourth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a token doubles every kind of counter on the target creature")
    void sacrificesTokenAndDoublesEachCounterKind() {
        Permanent cabinet = harness.addToBattlefieldAndReturn(player1, new ArcadeCabinet());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent token = addToken(player1);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cabinet), 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(cabinet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability requires a token to sacrifice")
    void requiresTokenToSacrifice() {
        Permanent cabinet = harness.addToBattlefieldAndReturn(player1, new ArcadeCabinet());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cabinet), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Test Token");
        tokenCard.setToken(true);
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }
}
