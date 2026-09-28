package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ScourgeOfNumai;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicCovenant.class, ScourgeOfNumai.class, GrizzlyBears.class, Forest.class,
        Ornithopter.class})
class DemonicCovenantTest extends BaseCardTest {

    @Test
    @DisplayName("Demons attacking a player draw a card and lose 1 life once per attacked player")
    void demonsAttackingDrawAndLoseLifeOncePerPlayer() {
        harness.addToBattlefield(player1, new DemonicCovenant());
        Permanent firstDemon = addCreatureReady(player1, new ScourgeOfNumai());
        Permanent secondDemon = addCreatureReady(player1, new ScourgeOfNumai());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstDemon),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondDemon)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Non-Demons do not trigger the attack ability")
    void nonDemonsDoNotTriggerAttackAbility() {
        harness.addToBattlefield(player1, new DemonicCovenant());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The end step creates a Demon, mills two cards, and sacrifices on an exact type match")
    void endStepCreatesAndSacrificesOnExactTypeMatch() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(covenant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Cards with different type combinations do not sacrifice the enchantment")
    void endStepKeepsCovenantWhenTypeCombinationsDiffer() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new Ornithopter();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(covenant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
