package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackWidowAgileAvenger.class, GrizzlyBears.class})
class BlackWidowAgileAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Grows and draws a card when an opponent draws their second card")
    void growsAndDrawsOnOpponentsSecondDraw() {
        Permanent blackWidow = harness.addToBattlefieldAndReturn(player1, new BlackWidowAgileAvenger());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        draw(player2);
        assertThat(gd.stack).isEmpty();

        draw(player2);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(blackWidow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);

        draw(player2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on its controller's draws or an opponent's first draw")
    void doesNotTriggerOnControllerOrFirstOpponentDraw() {
        Permanent blackWidow = harness.addToBattlefieldAndReturn(player1, new BlackWidowAgileAvenger());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(blackWidow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void draw(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
