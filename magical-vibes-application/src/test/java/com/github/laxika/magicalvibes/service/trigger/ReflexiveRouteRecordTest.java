package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * A reflexive triggered ability an effect handler builds ("when you do, ...") records what the ability that made it
 * was about, whichever way it reaches the stack: with nothing to choose, with a target or with a graveyard card
 * chosen as it is put there (CR 603.12, CR 603.3d), so an effect of it reads the event: "that spell's mana value" is the
 * mana value of the spell the ability that made it triggered for.
 *
 * <p>Each case puts a fixture creature with "Whenever this creature deals combat damage to a player, [continuation]"
 * on the battlefield, lets it deal combat damage, keeps a copy of the triggered ability's entry, resolves it, answers
 * the choices the reflexive ability asks, and compares the reflexive entry's record of the trigger event, field by
 * field, with the entry that made it.</p>
 */
@CardUsed({GrizzlyBears.class})
class ReflexiveRouteRecordTest extends BaseCardTest {

    private static final Pattern RECORD_FIELD_NAME = Pattern.compile("^(triggering|event)[A-Z].*|.*AtTrigger$");

    private static final Set<String> NAMED_RECORD_FIELDS = Set.of(
            "attackedTargetId", "attackingPermanentSnapshots", "defendingPlayerId", "combatOpponentPowerAtTrigger",
            "combatOpponentToughnessAtTrigger", "grantingPermanentId", "dyingPermanentManaValue", "linkedExiledCardId",
            "attachedPermanentSnapshot", "sourceGraveyardEntryVersion", "sourcePermanentSnapshot", "xValue",
            "alternateCost", "producedManaColors");

    private static final List<Field> RECORD_FIELDS = Arrays.stream(StackEntry.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
            .filter(field -> RECORD_FIELD_NAME.matcher(field.getName()).matches()
                    || NAMED_RECORD_FIELDS.contains(field.getName()))
            .sorted(Comparator.comparing(Field::getName))
            .peek(field -> field.setAccessible(true))
            .toList();

    enum Continuation {
        QUEUE_REFLEXIVE(QueueReflexiveAbilityEffect::new),
        REMOVE_COUNTER(effect -> new RemoveCounterFromSourceThenEffect(CounterType.PLUS_ONE_PLUS_ONE, effect)),
        EXILE_GRAVEYARD_CARDS(effect -> new ExileNCardsFromGraveyardThenEffect(1, effect)),
        DRAW_THEN(effect -> new DrawCardsThenEffect(1, effect));

        private final Function<CardEffect, CardEffect> wrap;

        Continuation(Function<CardEffect, CardEffect> wrap) {
            this.wrap = wrap;
        }
    }

    enum Route {
        PLAIN, TARGET, GRAVEYARD_CARD;

        CardEffect effect() {
            return switch (this) {
                case PLAIN -> new GainLifeEffect(1);
                case TARGET -> new DestroyTargetPermanentEffect();
                case GRAVEYARD_CARD -> ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build();
            };
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(Continuation.QUEUE_REFLEXIVE, Route.PLAIN),
                Arguments.of(Continuation.QUEUE_REFLEXIVE, Route.TARGET),
                Arguments.of(Continuation.QUEUE_REFLEXIVE, Route.GRAVEYARD_CARD),
                Arguments.of(Continuation.REMOVE_COUNTER, Route.PLAIN),
                Arguments.of(Continuation.REMOVE_COUNTER, Route.TARGET),
                Arguments.of(Continuation.REMOVE_COUNTER, Route.GRAVEYARD_CARD),
                Arguments.of(Continuation.EXILE_GRAVEYARD_CARDS, Route.PLAIN),
                Arguments.of(Continuation.EXILE_GRAVEYARD_CARDS, Route.GRAVEYARD_CARD),
                Arguments.of(Continuation.DRAW_THEN, Route.TARGET));
    }

    private Permanent target;

    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("cases")
    @DisplayName("A reflexive ability records the trigger event of the ability that made it on every route")
    void reflexiveAbilityRecordsTheTriggerEventOfTheAbilityThatMadeIt(Continuation continuation, Route route) {
        Card fixture = new Card();
        fixture.setName("Reflexive Fixture " + continuation + " " + route);
        fixture.setType(CardType.CREATURE);
        fixture.setManaCost("{1}");
        fixture.setPower(2);
        fixture.setToughness(2);
        fixture.addTrigger(Triggers.dealsCombatDamageToPlayer(), continuation.wrap.apply(route.effect()));

        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            graveyard.add(new GrizzlyBears());
        }
        harness.setGraveyard(player1, graveyard);
        target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        Permanent source = addCreatureReady(player1, fixture);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setAttacking(true);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());

        List<StackEntry> made = entriesOf(fixture);
        assertThat(made).as("the combat damage ability on the stack").hasSize(1);
        StackEntry parent = new StackEntry(made.getFirst());
        assertThat(parent.getEventValue()).as("the damage the ability records").isEqualTo(3);
        assertThat(parent.getTriggeringPlayerId()).as("the player the ability records").isEqualTo(player2.getId());

        harness.passBothPriorities();
        for (int round = 0; round < 8 && gd.interaction.isAwaitingInput(); round++) {
            answerOnce();
        }

        List<StackEntry> reflexive = entriesOf(fixture);
        assertThat(reflexive)
                .as("the reflexive ability on the stack; the game is waiting for %s", gd.interaction.activeInteraction())
                .hasSize(1);
        StackEntry entry = reflexive.getFirst();
        assertThat(entry).as("the reflexive ability is a new stack object").isNotSameAs(made.getFirst());
        if (route != Route.PLAIN) {
            assertThat(entry.getTargetId() != null
                    || entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty())
                    .as("the %s route chose a target", route).isTrue();
        }

        Map<String, String> expected = record(parent);
        Map<String, String> actual = record(entry);
        List<String> differences = new ArrayList<>();
        expected.forEach((field, value) -> {
            if (!value.equals(actual.get(field))) {
                differences.add(field + ": " + value + " -> " + actual.get(field));
            }
        });
        if (!differences.isEmpty()) {
            fail(continuation + " / " + route + ": the reflexive ability records the trigger event differently from the"
                    + " ability that made it (field: ability -> reflexive ability):\n  "
                    + String.join("\n  ", differences));
        }
    }

    @Test
    @DisplayName("A reflexive ability with a target reads \"that spell's mana value\" from the spell its maker triggered for")
    void targetedReflexiveAbilityReadsTheTriggeringSpell() {
        Card fixture = new Card();
        fixture.setName("Reflexive Spell Watcher");
        fixture.setType(CardType.ENCHANTMENT);
        fixture.setManaCost("{1}");
        fixture.addTrigger(Triggers.youCastSpell(), new QueueReflexiveAbilityEffect(
                new DealDamageToAnyTargetEffect(new TargetSpellManaValue())));
        harness.addToBattlefield(player1, fixture);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).as("the reflexive ability asks for its target").isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        List<StackEntry> reflexive = entriesOf(fixture);
        assertThat(reflexive).as("the reflexive ability on the stack").hasSize(1);
        assertThat(reflexive.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).as("damage equal to the spell's mana value").isEqualTo(lifeBefore - 2);
    }

    private void answerOnce() {
        List<Runnable> answers = new ArrayList<>();
        answers.add(() -> harness.handlePermanentChosen(player1, target.getId()));
        answers.add(() -> harness.handleMultiplePermanentsChosen(player1, List.of(target.getId())));
        List<UUID> graveyardCreature = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getId)
                .limit(1)
                .toList();
        answers.add(() -> harness.handleMultipleCardsChosen(player1, graveyardCreature));
        answers.add(() -> harness.handleGraveyardCardChosen(player1, 0));
        answers.add(() -> harness.handleMayAbilityChosen(player1, true));
        for (Runnable answer : answers) {
            try {
                answer.run();
                return;
            } catch (RuntimeException | AssertionError refused) {
                // The next kind of answer is tried.
            }
        }
        fail("no answer was accepted for " + gd.interaction.activeInteraction());
    }

    private List<StackEntry> entriesOf(Card fixture) {
        return gd.stack.stream()
                .filter(entry -> entry.getCard() != null && fixture.getName().equals(entry.getCard().getName()))
                .toList();
    }

    private Map<String, String> record(StackEntry entry) {
        Map<String, String> record = new LinkedHashMap<>();
        for (Field field : RECORD_FIELDS) {
            try {
                record.put(field.getName(), normalize(field.get(entry)));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return record;
    }

    private String normalize(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Permanent permanent) {
            return "P(" + permanent.getCard().getName() + ")";
        }
        if (value instanceof StackEntry) {
            return "S";
        }
        if (value instanceof Card card) {
            return "C(" + card.getName() + ")";
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::normalize).sorted().toList().toString();
        }
        if (value instanceof Map<?, ?> map) {
            return "M" + map.size();
        }
        return value.toString();
    }
}
