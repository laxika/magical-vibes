package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThopterMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself for the second card drawn each turn")
    void triggersOnSecondCardDrawn() {
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Thopter token when it dies")
    void deathCreatesThopterToken() {
        harness.addToBattlefield(player1, new ThopterMechanic());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(1);
        assertThat(thopters.getFirst().getCard().isToken()).isTrue();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
