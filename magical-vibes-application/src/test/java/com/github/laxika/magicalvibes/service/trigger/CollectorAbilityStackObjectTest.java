package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.rules.TapEvent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LivingDeath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The collectors outside the trigger registry put each triggered ability on the stack as one object of all its effects
 * (CR 603.3): an ability of two effects is one object an opponent can't respond between, and two abilities of one
 * permanent on one trigger are two objects, never joined into one.
 */
@CardUsed({GrizzlyBears.class, LivingDeath.class})
class CollectorAbilityStackObjectTest extends BaseCardTest {

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

    private static PermanentCounterSupport counterSupport() {
        return GameTestEngineContext.get().getBean(PermanentCounterSupport.class);
    }

    @Test
    @DisplayName("Two \"+1/+1 counters are put on this\" abilities are two stack objects")
    void twoPlusOneCountersOnThisAbilitiesAreTwoStackObjects() {
        Card card = watcher("Counter Watcher");
        card.addTrigger(Triggers.plusOneCountersPutOnThis(), new DrawCardEffect(1));
        card.addTrigger(Triggers.plusOneCountersPutOnThis(), new GainLifeEffect(1));
        Permanent watcher = addCreatureReady(player1, card);

        harness.inMutationScope(() -> counterSupport().firePlusOnePlusOneCounterTriggers(gd, watcher,
                player1.getId()));

        assertThat(entriesOf("Counter Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("\"Whenever a +1/+1 counter is put on this, draw a card and gain 1 life\" is one object per counter")
    void eachPlusOneCounterAbilityIsOneStackObjectPerCounter() {
        Card card = watcher("Each Counter Watcher");
        card.addTrigger(Triggers.plusOneCounterPutOnThis(), new DrawCardEffect(1), new GainLifeEffect(1));
        Permanent watcher = addCreatureReady(player1, card);

        harness.inMutationScope(() -> counterSupport().firePlusOnePlusOneCounterTriggers(gd, watcher,
                player1.getId(), List.of(), 2));

        assertThat(entriesOf("Each Counter Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Two \"counters are put on a creature you control\" abilities are two stack objects")
    void twoCountersOnCreatureYouControlAbilitiesAreTwoStackObjects() {
        Card card = watcher("Ally Counter Watcher");
        card.addTrigger(Triggers.countersPutOnCreatureYouControl(), new DrawCardEffect(1));
        card.addTrigger(Triggers.countersPutOnCreatureYouControl(), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> counterSupport().fireCounterPutOnControlledCreatureTriggers(gd, bears, 1,
                player1.getId()));

        assertThat(entriesOf("Ally Counter Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("Two \"when this reaches level 2\" abilities are two stack objects")
    void twoLevelTwoAbilitiesAreTwoStackObjects() {
        Card card = watcher("Level Watcher");
        card.addTrigger(Triggers.reachesLevelTwo(), new DrawCardEffect(1));
        card.addTrigger(Triggers.reachesLevelTwo(), new GainLifeEffect(1));
        Permanent watcher = addCreatureReady(player1, card);
        watcher.setCounterCount(CounterType.LEVEL, 1);

        harness.inMutationScope(() -> counterSupport().notifySelfCountersPlaced(gd, null, watcher,
                CounterType.LEVEL, 0, 1));

        assertThat(entriesOf("Level Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("\"Whenever an opponent shuffles their library, draw a card and gain 1 life\" is one stack object")
    void shuffleAbilityIsOneStackObject() {
        Card card = watcher("Shuffle Watcher");
        card.addTrigger(Triggers.opponentShufflesLibrary(), new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);

        harness.inMutationScope(() -> LibraryShuffleHelper.shuffleLibrary(gd, player2.getId()));

        assertThat(entriesOf("Shuffle Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Two \"whenever an opponent searches their library\" abilities are two stack objects")
    void twoSearchAbilitiesAreTwoStackObjects() {
        Card card = watcher("Search Watcher");
        card.addTrigger(Triggers.opponentSearchesTheirLibrary(), new DrawCardEffect(1));
        card.addTrigger(Triggers.opponentSearchesTheirLibrary(), new GainLifeEffect(1));
        addCreatureReady(player1, card);

        harness.inMutationScope(() -> LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gd,
                player2.getId()));

        assertThat(entriesOf("Search Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("Two \"when you cycle this card\" abilities are two stack objects")
    void twoSelfCycledAbilitiesAreTwoStackObjects() {
        Card card = watcher("Cycle Watcher");
        card.addTrigger(Triggers.youCycleThisCard(), new DrawCardEffect(1));
        card.addTrigger(Triggers.youCycleThisCard(), new GainLifeEffect(1));
        harness.setGraveyard(player1, List.of(card));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkCycleTriggers(gd, player1.getId(),
                card));

        assertThat(entriesOf("Cycle Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("\"Whenever a permanent you control becomes tapped, draw a card and gain 1 life\" is one stack object")
    void allyTappedAbilityIsOneStackObject() {
        Card card = watcher("Tap Watcher");
        card.addTrigger(Triggers.permanentYouControlBecomesTapped(), new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.inMutationScope(() -> gd.rulesEvents.occur(new TapEvent.BecameTapped(bears, player1.getId(), false)));

        assertThat(entriesOf("Tap Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"Whenever a permanent you control becomes untapped, draw a card and gain 1 life\" is one stack object")
    void allyUntappedAbilityIsOneStackObject() {
        Card card = watcher("Untap Watcher");
        card.addTrigger(Triggers.permanentYouControlBecomesUntapped(), new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> gd.rulesEvents.occur(new TapEvent.BecameUntapped(bears)));

        assertThat(entriesOf("Untap Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Two \"when you discard this card\" abilities are two stack objects")
    void twoSelfDiscardedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Discard Watcher");
        card.addTrigger(Triggers.youDiscardThisCard(), new DrawCardEffect(1));
        card.addTrigger(Triggers.youDiscardThisCard(), new GainLifeEffect(1));
        harness.setGraveyard(player1, List.of(card));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkDiscardTriggers(gd, player1.getId(),
                card));

        assertThat(entriesOf("Discard Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("Two \"when this exploits a creature\" abilities are two stack objects")
    void twoExploitAbilitiesAreTwoStackObjects() {
        Card card = watcher("Exploit Watcher");
        card.addTrigger(Triggers.exploits(), new DrawCardEffect(1));
        card.addTrigger(Triggers.exploits(), new GainLifeEffect(1));
        Permanent watcher = addCreatureReady(player1, card);

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkExploitTriggers(gd, card,
                player1.getId(), watcher.getId()));

        assertThat(entriesOf("Exploit Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("Two \"one or more creatures enter\" abilities that begin with the same effect each trigger once")
    void twoOneOrMoreEntersAbilitiesEachTrigger() {
        Card card = new Card();
        card.setName("Entry Watcher");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}");
        TriggerCondition entersYouControl = new TriggerCondition(
                Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL));
        card.addTrigger(entersYouControl, TriggerOptions.ONE_OR_MORE, new DrawCardEffect(1));
        card.addTrigger(entersYouControl, TriggerOptions.ONE_OR_MORE, new DrawCardEffect(1), new GainLifeEffect(1));
        harness.addToBattlefield(player1, card);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromHand(player1, new LivingDeath(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(entriesOf("Entry Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getTriggeringPermanentIds()).hasSize(2));
    }
}
