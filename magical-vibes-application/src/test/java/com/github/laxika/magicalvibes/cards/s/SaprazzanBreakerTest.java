package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaprazzanBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Milling a land makes Saprazzan Breaker unblockable this turn")
    void millingLandMakesItUnblockable() {
        Permanent breaker = addReadyBreaker();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addBlueMana();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(breaker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Milling a nonland does not make Saprazzan Breaker unblockable")
    void millingNonlandDoesNotMakeItUnblockable() {
        Permanent breaker = addReadyBreaker();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        addBlueMana();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(breaker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent breaker = addReadyBreaker();
        harness.setLibrary(player1, List.of(new Forest()));
        addBlueMana();

        activateAndResolve(breaker);
        assertThat(breaker.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(breaker.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadyBreaker() {
        return addCreatureReady(player1, new SaprazzanBreaker());
    }

    private void addBlueMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void activateAndResolve(Permanent breaker) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(breaker), null, null);
        harness.passBothPriorities();
    }
}
