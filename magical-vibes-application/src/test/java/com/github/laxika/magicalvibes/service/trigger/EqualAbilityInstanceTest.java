package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Equal abilities of one object are separate instances that each function on their own (CR 113.2c), so each keeps
 * its own "triggers only once each turn"; and an intervening "if" about the counters on a permanent is a check of the
 * game state as counters are put on it (CR 603.4), not of the counter that crossed the number.
 */
@CardUsed({GrizzlyBears.class})
class EqualAbilityInstanceTest extends BaseCardTest {

    private static final TriggerOptions ONCE_EACH_TURN = TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN);

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private void attackWith(int index) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(index)));
    }

    private void attackAgainWith(Permanent attacker, int index) {
        attacker.untap();
        attacker.setAttacking(false);
        attackWith(index);
    }

    @Test
    @DisplayName("Two equal printed \"whenever you attack\" abilities that trigger only once each turn each trigger")
    void twoEqualPrintedOnceEachTurnAbilitiesEachTrigger() {
        Card card = creature("Echoing Drummer");
        card.addTrigger(Triggers.youAttack(), ONCE_EACH_TURN, new DrawCardEffect(1));
        card.addTrigger(Triggers.youAttack(), ONCE_EACH_TURN, new DrawCardEffect(1));
        Permanent drummer = addCreatureReady(player1, card);

        attackWith(0);

        assertThat(entriesOf("Echoing Drummer")).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        attackAgainWith(drummer, 0);
        assertThat(entriesOf("Echoing Drummer")).isEmpty();
    }

    @Test
    @DisplayName("An ability a creature was given twice until end of turn triggers twice, each once each turn")
    void twoEqualTemporaryOnceEachTurnAbilitiesEachTrigger() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.attacks(), ONCE_EACH_TURN,
                new DrawCardEffect(1));
        Permanent bears = addCreatureReady(player1, creature("Twice Blessed"));
        bears.addTemporaryAbility(ability);
        bears.addTemporaryAbility(ability);

        attackWith(0);

        assertThat(entriesOf("Twice Blessed")).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        attackAgainWith(bears, 0);
        assertThat(entriesOf("Twice Blessed")).isEmpty();
    }

    @Test
    @DisplayName("\"Whenever counters are put on this, if it has two or more\" triggers for the third counter too")
    void counterInterveningIfIsTheStateNotTheCrossing() {
        Card card = creature("Patient Collector");
        card.addTrigger(Triggers.attacks(), new PutCountersOnSelfEffect(CounterType.CHARGE));
        card.addTrigger(Triggers.countersPutOnThis(), TriggerOptions.DEFAULT.withInterveningIf(
                new SourceCounterThreshold(2, CounterType.CHARGE)), new GainLifeEffect(1));
        Permanent collector = addCreatureReady(player1, card);
        harness.setLife(player1, 20);

        attackWith(0);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        attackAgainWith(collector, 0);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        attackAgainWith(collector, 0);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(collector.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
