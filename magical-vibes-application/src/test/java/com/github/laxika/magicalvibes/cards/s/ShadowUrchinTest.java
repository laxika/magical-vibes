package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShadowUrchinTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, it blights a creature you control")
    void attacksBlightsAControlledCreature() {
        Permanent urchin = addCreatureReady(player1, new ShadowUrchin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(urchin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When a countered creature you control dies, exiles one card per counter until your next end step")
    void counteredAllyDeathExilesCardsUntilNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ShadowUrchin());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        Card first = new Island();
        Card second = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(first, second));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId()).doesNotContainKey(second.getId());
    }

    @Test
    @DisplayName("Does not trigger when the dying creature has no counters")
    void counterlessAllyDeathDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ShadowUrchin());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card top = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(top);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }
}
