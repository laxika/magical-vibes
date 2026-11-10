package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The collectors judge and record a triggered ability by what it is, never by the class of one of its effects: an
 * ability goes on the stack with all its effects whatever they are (CR 603.3), its intervening "if" decides whether it
 * triggers (CR 603.4), and what its event was about is recorded on its stack entry whatever reads it.
 */
@CardUsed({GrizzlyBears.class, Shock.class, DarkBanishing.class})
class ClassKeyedRecordCollectorTest extends BaseCardTest {

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

    private StackEntry opponentSpellTargeting(UUID... targetIds) {
        StackEntry spell = new StackEntry(StackEntryType.INSTANT_SPELL, new Shock(), player2.getId(), "Shock",
                List.of(), (UUID) null, List.of(targetIds));
        gd.stack.add(spell);
        return spell;
    }

    @Test
    @DisplayName("\"Whenever you put one or more counters on another creature, draw a card\" triggers")
    void countersOnAnotherCreatureAbilityTriggersWhateverItsEffect() {
        Card card = watcher("Counter Scholar");
        card.addTrigger(Triggers.youPutCountersOnAnotherCreature(), new DrawCardEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> counterSupport().fireYouPutCountersOnAnotherCreatureTriggers(gd, bears,
                CounterType.PLUS_ONE_PLUS_ONE, 2, player1.getId()));

        assertThat(entriesOf("Counter Scholar")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("\"... put the same number and kind of counters on this and gain 1 life\" is one object of both effects")
    void sameCountersAbilityKeepsItsOtherEffect() {
        Card card = watcher("Counter Mimic");
        card.addTrigger(Triggers.youPutCountersOnAnotherCreature(), new PutSameCountersOnSourceEffect(),
                new GainLifeEffect(1));
        Permanent mimic = addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> counterSupport().fireYouPutCountersOnAnotherCreatureTriggers(gd, bears,
                CounterType.PLUS_ONE_PLUS_ONE, 2, player1.getId()));

        assertThat(entriesOf("Counter Mimic")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        harness.passBothPriorities();
        assertThat(mimic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("\"Whenever an opponent puts one or more counters on a creature they control, you gain 1 life\" triggers")
    void opponentPutsCountersAbilityTriggersWhateverItsEffect() {
        Card card = watcher("Counter Envier");
        card.addTrigger(Triggers.opponentPutsCountersOnCreatureTheyControl(), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> counterSupport().fireOpponentPutsCountersOnControlledCreatureTriggers(gd, bears,
                CounterType.PLUS_ONE_PLUS_ONE, 1, player2.getId()));

        assertThat(entriesOf("Counter Envier")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("\"Whenever a player chooses targets, if it has a single target, ...\" doesn't trigger for two targets")
    void targetChoiceAbilityJudgesItsInterveningIfAsItTriggers() {
        Card card = watcher("Single Target Watcher");
        card.addTrigger(Triggers.playerChoosesTargets(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringSpellMatches(new StackEntryIsSingleTargetPredicate())),
                new DrawCardEffect(1));
        addCreatureReady(player1, card);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        StackEntry twoTargets = opponentSpellTargeting(first.getId(), second.getId());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkTargetChoiceTriggers(gd, twoTargets));
        assertThat(entriesOf("Single Target Watcher")).isEmpty();

        StackEntry oneTarget = opponentSpellTargeting(first.getId());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkTargetChoiceTriggers(gd, oneTarget));
        assertThat(entriesOf("Single Target Watcher")).hasSize(1);
    }

    @Test
    @DisplayName("\"Whenever a player chooses targets, draw a card\" of a permanent that lost its abilities doesn't trigger")
    void targetChoiceAbilityOfPermanentWithoutAbilitiesDoesNotTrigger() {
        Card card = watcher("Silenced Target Watcher");
        card.addTrigger(Triggers.playerChoosesTargets(), new DrawCardEffect(1));
        Permanent silenced = addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        silenced.setLosesAllAbilitiesUntilEndOfTurn(true);
        StackEntry spell = opponentSpellTargeting(bears.getId());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkTargetChoiceTriggers(gd, spell));

        assertThat(entriesOf("Silenced Target Watcher")).isEmpty();
    }

    @Test
    @DisplayName("An Aura's \"when enchanted creature dies, exile this card\" leaves a card that left and came back")
    void enchantedCreatureDeathAbilityRecordsTheAurasGraveyardStay() {
        Card auraCard = new Card();
        auraCard.setName("Mourning Shroud");
        auraCard.setType(CardType.ENCHANTMENT);
        auraCard.setSubtypes(List.of(CardSubtype.AURA));
        auraCard.setManaCost("{1}");
        auraCard.addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), new ExileSourceCardFromGraveyardEffect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, auraCard);
        aura.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getName().equals("Mourning Shroud"));
        assertThat(entriesOf("Mourning Shroud")).singleElement().satisfies(entry ->
                assertThat(entry.sourceCardGraveyardEntryVersion())
                        .isEqualTo(gd.graveyardEntryVersion(auraCard.getId())));

        // The Aura leaves the graveyard and comes back before the ability resolves: a new object (CR 400.7).
        gd.markGraveyardExit(auraCard.getId());
        gd.markGraveyardEntry(auraCard);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getName().equals("Mourning Shroud"));
    }
}
