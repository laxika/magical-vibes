package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderManNoir.class, GrizzlyBears.class})
class SpiderManNoirTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on an attacking creature, then surveils for its counters")
    void counterAndSurveilUseAttackingCreature() {
        Permanent noir = addCreatureReady(player1, new SpiderManNoir());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        noir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(noir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
    }

    @Test
    @DisplayName("Does not trigger when multiple creatures attack")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new SpiderManNoir());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1, 2));

        assertThat(firstAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
