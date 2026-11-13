package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The watches' collectors hand their handler a whole triggered ability, and the handler puts it on the stack as one
 * object of all its effects (CR 603.3): an ability of two effects is one stack object an opponent can't respond
 * between, not two.
 */
@CardUsed({GrizzlyBears.class})
class TriggeredAbilityDispatchTest extends BaseCardTest {

    private static Card watcher(String name) {
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

    @Test
    @DisplayName("\"Whenever you gain life, draw a card and put a +1/+1 counter on this\" is one stack object")
    void lifeGainAbilityIsOneStackObject() {
        Card card = watcher("Life Watcher");
        card.addTrigger(Triggers.youGainLife(), new DrawCardEffect(1),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addCreatureReady(player1, card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.stack.add(TestStackDoor.triggered(new GrizzlyBears(), player1.getId(),
                "gain", new ArrayList<>(List.of(new GainLifeEffect(2)))));

        harness.passBothPriorities();

        assertThat(entriesOf("Life Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"Whenever another creature you control enters, draw a card and gain 1 life\" is one stack object")
    void anotherCreatureEntersAbilityIsOneStackObject() {
        Card card = watcher("Enter Watcher");
        card.addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(entriesOf("Enter Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"Whenever a creature dies, draw a card and gain 1 life\" is one stack object")
    void anyCreatureDiesAbilityIsOneStackObject() {
        Card card = watcher("Death Watcher");
        card.addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(entriesOf("Death Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }
}
