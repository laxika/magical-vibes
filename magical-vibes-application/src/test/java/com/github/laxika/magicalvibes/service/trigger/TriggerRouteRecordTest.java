package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TransformChosenPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.DayNightService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Guards the rule that every route by which a triggered ability reaches the stack records the trigger event as the
 * ability that chooses nothing does. A triggered ability that asks for nothing is put on the stack directly; one with
 * a lone "you may", one target, two target groups, a graveyard target, a mode chosen as it triggers or such a mode
 * with a target is parked, asks its controller, and reaches the stack later from a queue or an interaction handler.
 * The event's record has to survive that detour: the engine hands it off with {@code GameData.handOffTriggerRecord}
 * and the stack entry built at the end of the route takes it over with {@code StackEntry.carryTriggerRecord}.
 *
 * <p>Each case builds one world with two fixture cards on the same trigger, a plain one ({@link GainLifeEffect}) and
 * one {@link Variant}, makes the {@link Watch}'s event happen, answers every choice without passing priority, and
 * compares the two stack entries the engine produced field by field (see {@link #RECORD_FIELDS}). Values are compared
 * by role (the source, a player, a target), not by raw id. Two differences are allowed, both for a variant that
 * chooses a target: {@code nonTargeting} going from true to false, as the chosen target replaces the plain ability's
 * non-targeting "that player" or "that creature", and {@code xValue} going from 0 to the event's value, the X a
 * target restricted by "that much" is chosen against (the second is not allowed when the environment variable
 * {@value #STRICT_VARIABLE} is set).
 *
 * <p>Measuring mode: set the environment variable {@value #MEASURE_VARIABLE} to a file path and each case appends one
 * line to that file instead of asserting, {@code <watch> / <variant>: SAME}, {@code <watch> / <variant>: [field: plain
 * -> variant, ...]} or {@code <watch> / <variant>: NO ENTRY (plain=<n>, variant=<m>) <what the game is waiting for>},
 * so the lines can be counted before and after an engine change. Pairs listed in {@link #EXCLUDED} are skipped when
 * asserting and still measured.
 *
 * <p>Adding a watch is one entry in {@link #watches()}.
 */
@CardUsed({GrizzlyBears.class, Forest.class})
class TriggerRouteRecordTest extends BaseCardTest {

    private static final String MEASURE_VARIABLE = "MV_ROUTE_PROBE_OUT";
    private static final String STRICT_VARIABLE = "MV_ROUTE_PROBE_STRICT";

    private static final String GAIN_LABEL = "You gain 1 life";
    private static final String DESTROY_LABEL = "Destroy target creature";

    private static final Pattern RECORD_FIELD_NAME = Pattern.compile("^(triggering|event)[A-Z].*|.*AtTrigger$");

    private static final Set<String> NAMED_RECORD_FIELDS = Set.of(
            "attackedTargetId", "attackingPermanentSnapshots", "defendingPlayerId", "combatOpponentPowerAtTrigger",
            "combatOpponentToughnessAtTrigger", "grantingPermanentId", "dyingPermanentManaValue", "linkedExiledCardId",
            "attachedPermanentSnapshot", "sourceGraveyardEntryVersion", "sourcePermanentSnapshot",
            "onceEachTurnActionKey", "xValue", "activePlayerId", "alternateCost", "producedManaColors",
            "kicked", "spectacle", "collectEvidenceCostPaid", "waterbendCostPaid", "revealCardFromHandCostPaid",
            "controlledDragonAsCast", "giftPromised", "sacrificedToughness", "repeatedAdditionalCosts",
            "convokeCreatureIds");

    /** The fields of {@link StackEntry} that make up the trigger event's record, besides {@code nonTargeting}. */
    private static final List<Field> RECORD_FIELDS = Arrays.stream(StackEntry.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
            .filter(field -> RECORD_FIELD_NAME.matcher(field.getName()).matches()
                    || NAMED_RECORD_FIELDS.contains(field.getName()))
            .sorted(Comparator.comparing(Field::getName))
            .peek(field -> field.setAccessible(true))
            .toList();

    /** The trigger-choice queue pumps of {@link TriggerCollectionService}: {@code processNext*(GameData)}. */
    private static final List<Method> QUEUE_PUMPS = Arrays.stream(TriggerCollectionService.class.getMethods())
            .filter(method -> method.getName().startsWith("processNext")
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == GameData.class)
            .sorted(Comparator.comparing(Method::getName))
            .toList();

    /**
     * The (watch, variant) pairs that reach no stack entry, as {@code "<watch> / <variant>"} to a one-line reason.
     * They are skipped when asserting and still written in measuring mode.
     */
    private static final Map<String, String> EXCLUDED = Map.of(
            "a token is created / GRAVE", "the token collector has no graveyard-card target route (no card prints "
                    + "one): the ability is removed for want of a permanent target",
            "you discard a card / GRAVE", "the discard collector has no graveyard-card target route (no card prints "
                    + "one): the ability isn't put on the stack");

    /** The way the fixture ability is written beside the plain one. */
    enum Variant {
        MAY(false, null),
        TARGET(true, null),
        TWO_GROUPS(true, null),
        GRAVE(true, null),
        MODAL(false, GAIN_LABEL),
        MODAL_TARGET(true, DESTROY_LABEL);

        private final boolean choosesTarget;
        private final String modeLabel;

        Variant(boolean choosesTarget, String modeLabel) {
            this.choosesTarget = choosesTarget;
            this.modeLabel = modeLabel;
        }
    }

    /**
     * One trigger event: the trigger the fixture cards are written on, the kind of card that hosts it, and the
     * scenario that puts the two fixture cards (plain first) where they see the event and makes it happen.
     */
    record Watch(String name, Supplier<Trigger> trigger, TriggerOptions options, CardType host,
                 List<CardSubtype> subtypes, BiConsumer<TriggerRouteRecordTest, List<Card>> scenario) {

        /** The same watch with its ability written with {@code options}, as "one or more" abilities are. */
        Watch with(TriggerOptions options) {
            return new Watch(name, trigger, options, host, subtypes, scenario);
        }

        void addAbility(Card card, CardEffect... effects) {
            card.addTrigger(trigger.get(), options, effects);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static Watch watch(String name, Supplier<Trigger> trigger, CardType host,
                               BiConsumer<TriggerRouteRecordTest, List<Card>> scenario, CardSubtype... subtypes) {
        return new Watch(name, trigger, TriggerOptions.DEFAULT, host, List.of(subtypes), scenario);
    }

    static List<Watch> watches() {
        return List.of(
                watch("self attack", Triggers::attacks, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAttack),
                watch("equipment attack", Triggers::attacks, CardType.ARTIFACT,
                        TriggerRouteRecordTest::equippedCreatureAttacks, CardSubtype.EQUIPMENT),
                watch("you attack", Triggers::youAttack, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("a creature you control attacks", Triggers::creatureYouControlAttacks, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("attacks and isn't blocked", Triggers::attacksUnblocked, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAttackUnblocked),
                watch("this blocks", Triggers::blocks, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBlock),
                watch("this becomes blocked", Triggers::becomesBlocked, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBecomeBlocked),
                watch("deals combat damage to a player", Triggers::dealsCombatDamageToPlayer, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("equipped creature deals combat damage to a player", Triggers::dealsCombatDamageToPlayer,
                        CardType.ARTIFACT, TriggerRouteRecordTest::equippedCreatureDealsCombatDamageToPlayer,
                        CardSubtype.EQUIPMENT),
                watch("own enters", () -> Triggers.enters(ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesEnter),
                watch("own enters, cast with its costs paid", () -> Triggers.enters(ObjectRelation.THIS),
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesEnterWithCostsPaid),
                watch("a creature enters", () -> Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureEnters),
                watch("own death", () -> Triggers.dies(ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDestroyed),
                watch("a creature dies", () -> Triggers.dies(Triggers.CREATURE, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsDestroyed),
                watch("your upkeep", () -> Triggers.upkeep(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::upkeepBegins),
                watch("you cast a spell", () -> Triggers.youCastSpell().anySpell(), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youCastACreatureSpell),
                watch("you gain life", Triggers::youGainLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new GainLifeEffect(3))),
                watch("your end step", () -> Triggers.endStep(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.POSTCOMBAT_MAIN, TurnStep.END_STEP)),
                watch("your draw step", () -> Triggers.drawStep(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.UPKEEP, TurnStep.DRAW)),
                watch("beginning of combat", () -> Triggers.beginningOfCombat(ActivePlayer.YOU),
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.PRECOMBAT_MAIN,
                                TurnStep.BEGINNING_OF_COMBAT)),
                watch("you lose life", Triggers::youLoseLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER))),
                watch("an opponent gains life", Triggers::opponentGainsLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new GainLifeEffect(new Fixed(1), GainLifeRecipient.OPPONENT))),
                watch("an opponent loses life", Triggers::opponentLosesLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT))),
                watch("you draw a card", Triggers::youDrawCard, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new DrawCardEffect(1))),
                watch("you discard a card", Triggers::youDiscardCard, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER, true))),
                watch("counters put on this", Triggers::countersPutOnThis, CardType.CREATURE,
                        TriggerRouteRecordTest::countersArePutOnFixtures),
                watch("counters put on a creature you control", Triggers::countersPutOnCreatureYouControl,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("becomes tapped", Triggers::becomesTapped, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAttack),
                watch("becomes untapped", Triggers::becomesUntapped, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesUntap),
                watch("a permanent you control becomes tapped", Triggers::permanentYouControlBecomesTapped,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("own sacrifice", () -> Triggers.sacrificed(ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreSacrificed),
                watch("you sacrifice a permanent", () -> Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsSacrificed),
                watch("own leaves the battlefield", () -> Triggers.leavesBattlefield(ObjectRelation.THIS),
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesAreDestroyed),
                watch("a creature leaves the battlefield",
                        () -> Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsDestroyed),
                watch("becomes the target of a spell or ability", Triggers::becomesTargetOfSpellOrAbility,
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesBecomeSpellTargets),
                watch("a creature becomes the target of a spell or ability",
                        Triggers::creatureBecomesTargetOfSpellOrAbility, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBecomesSpellTarget),
                watch("is dealt damage", Triggers::dealtDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDealtCombatDamage),
                watch("deals damage", Triggers::dealsDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("you are dealt damage", Triggers::youAreDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))),
                watch("an opponent is dealt damage", Triggers::opponentDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))),
                watch("an opponent casts a spell", () -> Triggers.opponentCastsSpell().anySpell(),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::opponentCastsACreatureSpell),
                watch("a land enters under your control",
                        () -> Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::landEnters),
                watch("you play a land", Triggers::youPlayLand, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youPlayALand),
                watch("a creature blocks", Triggers::creatureBlocks, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBlocks),
                watch("a creature you control becomes blocked", Triggers::creatureYouControlBecomesBlocked,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureBecomesBlocked),
                watch("a creature you control deals combat damage to a player",
                        Triggers::creatureYouControlDealsCombatDamageToPlayer, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("you tap a land for mana", Triggers::youTapLandForMana, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youTapALandForMana),
                watch("you scry", Triggers::youScry, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperLooksAtLibrary(cards, new ScryEffect(1))),
                watch("you surveil", Triggers::youSurveil, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperLooksAtLibrary(cards, new SurveilEffect(1))),
                watch("a token is created",
                        () -> Triggers.tokensCreated(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new CreateTokenEffect(
                                1, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), false)))
                        .with(TriggerOptions.ONE_OR_MORE),
                watch("turned face up", () -> Triggers.turnedFaceUp(null, ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreTurnedFaceUp),
                watch("transforms into its back face", () -> Triggers.transformsToBackFace(ObjectRelation.THIS),
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesTransform),
                watch("chaos ensues", Triggers::chaosEnsues, CardType.PLANE,
                        TriggerRouteRecordTest::chaosEnsues),
                watch("day becomes night", Triggers::dayBecomesNightOrNightBecomesDay, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::dayBecomesNight),
                watch("you roll a die", Triggers::youRollDie, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new RollD6Effect(new Fixed(1), List.of()))));
    }

    static Stream<Arguments> cases() {
        return watches().stream().flatMap(watch ->
                Arrays.stream(Variant.values()).map(variant -> Arguments.of(watch, variant)));
    }

    private Variant variant;
    private Permanent firstTarget;
    private Permanent secondTarget;
    private List<String> fixtureNames = List.of();
    private final List<String> refusals = new ArrayList<>();
    private final Map<UUID, UUID> cardOfPermanent = new HashMap<>();

    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("cases")
    void everyRouteRecordsTheTriggerEventAsThePlainAbilityDoes(Watch watch, Variant variant) {
        String label = watch.name() + " / " + variant;
        String measureFile = System.getenv(MEASURE_VARIABLE);
        boolean measuring = measureFile != null && !measureFile.isBlank();
        if (!measuring) {
            String reason = EXCLUDED.get(label);
            Assumptions.assumeTrue(reason == null, () -> label + " is excluded: " + reason);
        }

        this.variant = variant;
        Card plain = fixture(watch, "plain");
        watch.addAbility(plain, new GainLifeEffect(1));
        Card other = variantCard(watch, variant);
        fixtureNames = List.of(plain.getName(), other.getName());
        world();

        String scenarioFailure = "";
        try {
            watch.scenario().accept(this, List.of(plain, other));
        } catch (RuntimeException | AssertionError e) {
            if (!measuring) {
                throw e;
            }
            scenarioFailure = " (scenario threw " + e + ")";
        }
        answerAll();

        List<StackEntry> plainEntries = entriesOf(plain.getName());
        List<StackEntry> otherEntries = entriesOf(other.getName());
        boolean oneEach = plainEntries.size() == 1 && otherEntries.size() == 1;
        List<String> differences = oneEach
                ? differences(plainEntries.getFirst(), otherEntries.getFirst(), variant)
                : List.of();

        if (measuring) {
            String outcome = !oneEach
                    ? "NO ENTRY (plain=" + plainEntries.size() + ", variant=" + otherEntries.size() + ") "
                            + waitingFor()
                    : differences.isEmpty() ? "SAME" : differences.toString();
            write(Path.of(measureFile), label + ": " + outcome + scenarioFailure);
            return;
        }

        assertThat(plainEntries)
                .as("%s: stack entries of the plain ability; the game is waiting for %s", label, waitingFor())
                .hasSize(1);
        assertThat(otherEntries)
                .as("%s: stack entries of the %s ability; the game is waiting for %s", label, variant, waitingFor())
                .hasSize(1);
        if (!differences.isEmpty()) {
            fail(label + ": the " + variant + " route records the trigger event differently from the plain"
                    + " ability (field: plain -> variant):\n  " + String.join("\n  ", differences));
        }
    }

    private static Card fixture(Watch watch, String suffix) {
        return card(watch.name() + " " + suffix, watch.host(), watch.subtypes().toArray(CardSubtype[]::new));
    }

    private static Card card(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        if (type != CardType.LAND) {
            card.setManaCost("{1}");
        }
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(4);
        }
        return card;
    }

    /** The watch's ability written the {@code variant} way. */
    private static Card variantCard(Watch watch, Variant variant) {
        Card card = fixture(watch, variant.name().toLowerCase());
        switch (variant) {
            case MAY -> watch.addAbility(card, new MayEffect(new GainLifeEffect(1), "Gain?"));
            case TARGET -> {
                CardEffect boost = new BoostTargetCreatureEffect(1, 1);
                watch.addAbility(card, boost);
                card.target(TargetFilters.creature()).bindEffect(boost);
            }
            case TWO_GROUPS -> {
                CardEffect destroy = new DestroyTargetPermanentEffect();
                CardEffect boost = new BoostTargetCreatureEffect(1, 1);
                watch.addAbility(card, destroy, boost);
                card.target(TargetFilters.creature()).bindEffect(destroy);
                card.target(TargetFilters.creature()).bindEffect(boost);
            }
            case GRAVE -> watch.addAbility(card, ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .filter(new CardTypePredicate(CardType.CREATURE))
                    .targetGraveyard(true)
                    .build());
            case MODAL -> watch.addAbility(card, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption(GAIN_LABEL, new GainLifeEffect(1)),
                    new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1))))));
            case MODAL_TARGET -> watch.addAbility(card, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(
                    List.of(new ChooseOneEffect.ChooseOneOption(DESTROY_LABEL,
                                    new DestroyTargetPermanentEffect(), TargetFilters.creature()),
                            new ChooseOneEffect.ChooseOneOption(GAIN_LABEL, new GainLifeEffect(1))))));
        }
        return card;
    }

    /**
     * Creature cards in player1's graveyard for the graveyard target and two tapped creatures of player2 for the
     * permanent targets, at indices 0 and 1 of player2's battlefield.
     */
    private void world() {
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graveyard.add(new GrizzlyBears());
        }
        harness.setGraveyard(player1, graveyard);
        firstTarget = addCreatureReady(player2, new GrizzlyBears());
        firstTarget.tap();
        secondTarget = addCreatureReady(player2, new GrizzlyBears());
        secondTarget.tap();
    }

    private List<Permanent> fixtureCreatures(List<Card> cards) {
        List<Permanent> permanents = new ArrayList<>();
        for (Card card : cards) {
            permanents.add(addCreatureReady(player1, card));
        }
        rememberPermanents();
        return permanents;
    }

    private void fixturePermanents(List<Card> cards) {
        cards.forEach(card -> harness.addToBattlefield(player1, card));
        rememberPermanents();
    }

    /** Notes which card each permanent of player1 is, so a permanent that left is still known by its card. */
    private void rememberPermanents() {
        gd.playerBattlefields.get(player1.getId())
                .forEach(permanent -> cardOfPermanent.put(permanent.getId(), permanent.getCard().getId()));
    }

    private void mainPhaseOf(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private static <T> T bean(Class<T> type) {
        return GameTestEngineContext.get().getBean(type);
    }

    private void destroy(Permanent permanent) {
        PermanentRemovalService removal = bean(PermanentRemovalService.class);
        harness.inMutationScope(() -> removal.tryDestroyPermanent(gd, permanent));
    }

    private void fixturesAttack(List<Card> cards) {
        int count = fixtureCreatures(cards).size();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            indices.add(i);
        }
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, indices));
    }

    private void equippedCreatureAttacks(List<Card> cards) {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        for (Card card : cards) {
            addCreatureReady(player1, card).setAttachedTo(attacker.getId());
        }
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0)));
    }

    private void anotherCreatureAttacks(List<Card> cards) {
        addCreatureReady(player1, new GrizzlyBears());
        fixturePermanents(cards);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0)));
    }

    private void fixturesAttackUnblocked(List<Card> cards) {
        fixtureCreatures(cards).forEach(permanent -> permanent.setAttacking(true));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
    }

    private void fixturesBlock(List<Card> cards) {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        List<BlockerAssignment> assignments = new ArrayList<>();
        for (Permanent blocker : fixtureCreatures(cards)) {
            assignments.add(new BlockerAssignment(
                    gd.playerBattlefields.get(player1.getId()).indexOf(blocker), attackerIndex));
        }
        harness.forceActivePlayer(player2);
        prepareDeclareBlockers(player2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player1, assignments));
    }

    private void anotherCreatureBlocks(List<Card> cards) {
        fixturePermanents(cards);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        List<BlockerAssignment> assignments = List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        harness.forceActivePlayer(player2);
        prepareDeclareBlockers(player2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player1, assignments));
    }

    /** Each of {@code attackers} is blocked by a creature of player2 of its own. */
    private void blockEach(List<Permanent> attackers) {
        List<BlockerAssignment> assignments = new ArrayList<>();
        for (Permanent attacker : attackers) {
            attacker.setAttacking(true);
            Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
            assignments.add(new BlockerAssignment(
                    gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                    gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        }
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, assignments));
    }

    private void fixturesBecomeBlocked(List<Card> cards) {
        blockEach(fixtureCreatures(cards));
    }

    private void anotherCreatureBecomesBlocked(List<Card> cards) {
        fixturePermanents(cards);
        blockEach(List.of(addCreatureReady(player1, new GrizzlyBears())));
    }

    private void fixturesAreDealtCombatDamage(List<Card> cards) {
        blockEach(fixtureCreatures(cards));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    private void fixturesDealCombatDamageToPlayer(List<Card> cards) {
        fixtureCreatures(cards).forEach(permanent -> permanent.setAttacking(true));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    private void equippedCreatureDealsCombatDamageToPlayer(List<Card> cards) {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        for (Card card : cards) {
            addCreatureReady(player1, card).setAttachedTo(attacker.getId());
        }
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    private void anotherCreatureDealsCombatDamageToPlayer(List<Card> cards) {
        fixturePermanents(cards);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    private void fixturesEnter(List<Card> cards) {
        for (Card card : cards) {
            harness.enterBattlefieldAndReturn(player1, card);
            answerAll();
        }
    }

    /** The fixtures enter as permanents whose spells were kicked and had their other optional costs paid. */
    private void fixturesEnterWithCostsPaid(List<Card> cards) {
        BattlefieldEntryService entry = bean(BattlefieldEntryService.class);
        TriggerCollectionService collection = bean(TriggerCollectionService.class);
        for (Card card : cards) {
            Permanent permanent = new Permanent(card);
            permanent.setKicked(true);
            permanent.setSpectacle(true);
            permanent.setCollectEvidenceCostPaid(true);
            permanent.setWaterbendCostPaid(true);
            permanent.setRevealCardFromHandCostPaid(true);
            permanent.setControlledDragonAsCast(true);
            permanent.setGiftPromised(true);
            harness.inMutationScope(() -> {
                entry.putPermanentOntoBattlefield(gd, player1.getId(), permanent);
                entry.processCreatureETBEffects(gd, player1.getId(), card, null, false);
                collection.processNextTriggeredModalTrigger(gd);
            });
            answerAll();
        }
    }

    private void anotherCreatureEnters(List<Card> cards) {
        fixturePermanents(cards);
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
    }

    private void fixturesAreDestroyed(List<Card> cards) {
        for (Permanent permanent : fixtureCreatures(cards)) {
            destroy(permanent);
            answerAll();
        }
    }

    private void anotherCreatureIsDestroyed(List<Card> cards) {
        fixturePermanents(cards);
        destroy(addCreatureReady(player2, new GrizzlyBears()));
    }

    private void fixturesAreSacrificed(List<Card> cards) {
        for (Permanent permanent : fixtureCreatures(cards)) {
            harness.sacrifice(permanent);
            answerAll();
        }
    }

    private void anotherCreatureIsSacrificed(List<Card> cards) {
        fixturePermanents(cards);
        harness.sacrifice(addCreatureReady(player1, new GrizzlyBears()));
    }

    private void upkeepBegins(List<Card> cards) {
        fixturePermanents(cards);
        advanceToUpkeep(player1);
    }

    /** Player1's {@code step} begins, reached by passing priority from {@code from} of the same turn. */
    private void stepBegins(List<Card> cards, TurnStep from, TurnStep step) {
        fixturePermanents(cards);
        addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(from);
        harness.clearPriorityPassed();
        harness.passUntil(player1, step);
    }

    private void youCastACreatureSpell(List<Card> cards) {
        fixturePermanents(cards);
        mainPhaseOf(player1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
    }

    private void opponentCastsACreatureSpell(List<Card> cards) {
        fixturePermanents(cards);
        mainPhaseOf(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
    }

    /**
     * A helper creature of player1 enters and its own enters ability, {@code effect}, resolves: only that ability
     * resolves, so what the fixture cards trigger on it stays on the stack.
     */
    private void helperAbilityResolves(List<Card> cards, CardEffect effect) {
        fixturePermanents(cards);
        helperEntersAndItsAbilityResolves(effect);
    }

    private void helperEntersAndItsAbilityResolves(CardEffect effect) {
        Card helper = card("Helper", CardType.CREATURE);
        helper.addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        mainPhaseOf(player1);
        harness.enterBattlefieldAndReturn(player1, helper);
        harness.passBothPriorities();
    }

    private void countersArePutOnFixtures(List<Card> cards) {
        fixtureCreatures(cards);
        helperEntersAndItsAbilityResolves(new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, Triggers.CREATURE));
    }

    /** The helper scries or surveils 1 and player1 keeps the card on top. */
    private void helperLooksAtLibrary(List<Card> cards, CardEffect effect) {
        helperAbilityResolves(cards, effect);
        if (gd.interaction.isAwaitingInput()) {
            try {
                gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of(), null));
            } catch (RuntimeException | AssertionError e) {
                refusals.add("scry order: " + e.getMessage());
            }
        }
    }

    private void fixturesUntap(List<Card> cards) {
        fixtureCreatures(cards).forEach(Permanent::tap);
        harness.performUntapStep(player1);
    }

    private void castBoostAt(Permanent target) {
        Card boost = card("Boost", CardType.INSTANT);
        boost.addSpellEffect(new BoostTargetCreatureEffect(1, 1));
        mainPhaseOf(player1);
        harness.setHand(player1, List.of(boost));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
    }

    private void fixturesBecomeSpellTargets(List<Card> cards) {
        for (Permanent permanent : fixtureCreatures(cards)) {
            castBoostAt(permanent);
            answerAll();
        }
    }

    private void anotherCreatureBecomesSpellTarget(List<Card> cards) {
        fixturePermanents(cards);
        castBoostAt(addCreatureReady(player1, new GrizzlyBears()));
    }

    private void landEnters(List<Card> cards) {
        fixturePermanents(cards);
        harness.enterLandBattlefieldAndReturn(player1, card("Fixture Land", CardType.LAND));
    }

    private void youPlayALand(List<Card> cards) {
        fixturePermanents(cards);
        mainPhaseOf(player1);
        harness.setHand(player1, List.of(card("Fixture Land", CardType.LAND)));
        harness.playLand(player1, 0);
    }

    private void youTapALandForMana(List<Card> cards) {
        fixturePermanents(cards);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        mainPhaseOf(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));
    }

    private void fixturesAreTurnedFaceUp(List<Card> cards) {
        TurnFaceUpCopyService turnUp = bean(TurnFaceUpCopyService.class);
        List<Permanent> permanents = fixtureCreatures(cards);
        permanents.forEach(permanent -> permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE)));
        for (Permanent permanent : permanents) {
            harness.inMutationScope(() -> turnUp.turnFaceUpWithoutCost(gd, permanent));
            answerAll();
        }
    }

    /** Each fixture card is the back face of a front face of the same name, which transforms. */
    private void fixturesTransform(List<Card> cards) {
        TransformChosenPermanentEffectHandler transformer = bean(TransformChosenPermanentEffectHandler.class);
        for (Card back : cards) {
            Card front = card(back.getName(), CardType.CREATURE);
            front.setBackFaceCard(back);
            Permanent permanent = addCreatureReady(player1, front);
            harness.inMutationScope(() -> transformer.transform(gd, permanent.getId()));
            answerAll();
        }
    }

    private void dayBecomesNight(List<Card> cards) {
        fixturePermanents(cards);
        gd.dayNight = DayNight.DAY;
        DayNightService dayNight = bean(DayNightService.class);
        harness.inMutationScope(() -> dayNight.becomeNight(gd));
    }

    /** Each fixture card is a face-up plane of player1 whose chaos ability triggers. */
    private void chaosEnsues(List<Card> cards) {
        PlanechaseService planar = bean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        mainPhaseOf(player1);
        for (Card card : cards) {
            PlanarObject plane = new PlanarObject(card, gd.nextTimestamp());
            gd.planechase.faceUp.add(plane);
            harness.inMutationScope(() -> planar.trigger(gd, plane, Triggers.chaosEnsues(), player1.getId()));
            answerAll();
        }
    }

    /**
     * Answers every choice the fixture abilities ask for, and has every queued trigger choice asked, until neither
     * changes anything. Never passes priority: that would resolve the entries being compared.
     */
    private void answerAll() {
        rememberPermanents();
        for (int round = 0; round < 60; round++) {
            if (gd.interaction.isAwaitingInput()) {
                if (!answerOnce()) {
                    return;
                }
                continue;
            }
            int stackBefore = gd.stack.size();
            pumpQueues();
            if (!gd.interaction.isAwaitingInput() && gd.stack.size() == stackBefore) {
                return;
            }
        }
    }

    private void pumpQueues() {
        TriggerCollectionService collection = bean(TriggerCollectionService.class);
        for (Method pump : QUEUE_PUMPS) {
            if (gd.interaction.isAwaitingInput()) {
                return;
            }
            try {
                harness.inMutationScope(() -> {
                    try {
                        pump.invoke(collection, gd);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(e);
                    }
                });
            } catch (RuntimeException | AssertionError ignored) {
                // A pump with nothing queued, or one whose queue is not ready, changes nothing.
            }
        }
    }

    /** Gives the pending choice the first answer it accepts; false when it accepts none. */
    private boolean answerOnce() {
        List<Runnable> answers = new ArrayList<>();
        if (variant.modeLabel != null) {
            answers.add(() -> harness.handleListChoice(player1, variant.modeLabel));
        }
        answers.add(() -> harness.handlePermanentChosen(player1, firstTarget.getId()));
        answers.add(() -> harness.handlePermanentChosen(player1, secondTarget.getId()));
        answers.add(() -> harness.handleMultiplePermanentsChosen(player1, List.of(firstTarget.getId())));
        answers.add(() -> harness.handleMultiplePermanentsChosen(player1, List.of(secondTarget.getId())));
        List<UUID> graveyardCreature = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getId)
                .limit(1)
                .toList();
        answers.add(() -> harness.handleMultipleCardsChosen(player1, graveyardCreature));
        answers.add(() -> harness.handleGraveyardCardChosen(player1, 0));
        answers.add(() -> harness.handleMayAbilityChosen(player1, true));

        List<String> refused = new ArrayList<>();
        for (Runnable answer : answers) {
            try {
                answer.run();
                refusals.clear();
                return true;
            } catch (RuntimeException | AssertionError e) {
                refused.add(String.valueOf(e.getMessage()));
            }
        }
        refusals.clear();
        refusals.addAll(refused);
        return false;
    }

    private String waitingFor() {
        if (!gd.interaction.isAwaitingInput()) {
            return "nothing" + pendingQueues() + "; the game log ends " + gd.gameLog.stream()
                    .skip(Math.max(0, gd.gameLog.size() - 5))
                    .map(entry -> entry.plainText())
                    .toList();
        }
        Object interaction = gd.interaction.activeInteraction();
        Object context = gd.interaction.permanentChoiceContext();
        return (interaction == null ? "an unknown interaction" : interaction.getClass().getSimpleName())
                + (context == null ? "" : " " + context.getClass().getSimpleName())
                + (refusals.isEmpty() ? "" : ", every answer refused: " + String.join(" | ", refusals)
                        .replace('\n', ' '));
    }

    /** The non-empty {@code pending*} collections of the game, for a case that ended without its entry. */
    private String pendingQueues() {
        List<String> queues = new ArrayList<>();
        for (Field field : GameData.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !field.getName().toLowerCase().contains("pending")) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(gd);
                int size = value instanceof Collection<?> collection ? collection.size()
                        : value instanceof Map<?, ?> map ? map.size() : 0;
                if (size > 0) {
                    queues.add(field.getName() + "=" + size);
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // A field that cannot be read is left out of the diagnostic.
            }
        }
        return queues.isEmpty() ? "" : " (queued: " + String.join(", ", queues) + ")";
    }

    private static void write(Path file, String line) {
        try {
            Files.writeString(file, line.replace('\n', ' ') + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<StackEntry> entriesOf(String cardName) {
        return gd.stack.stream()
                .filter(entry -> entry.getCard() != null && cardName.equals(entry.getCard().getName()))
                .toList();
    }

    private List<String> differences(StackEntry plain, StackEntry other, Variant variant) {
        Map<String, String> plainRecord = record(plain);
        Map<String, String> otherRecord = record(other);
        List<String> differences = new ArrayList<>();
        for (Map.Entry<String, String> field : plainRecord.entrySet()) {
            String otherValue = otherRecord.get(field.getKey());
            if (field.getValue().equals(otherValue)) {
                continue;
            }
            boolean targetReplacesThatObject = field.getKey().equals("nonTargeting") && variant.choosesTarget
                    && field.getValue().equals("true") && "false".equals(otherValue);
            // The X of a chosen target or graveyard card is the event's value: a target restricted by "that much" or
            // "that spell's mana value" is chosen against it, where the plain entry keeps it as its event value only.
            boolean choiceCarriesEventValue = System.getenv(STRICT_VARIABLE) == null
                    && field.getKey().equals("xValue") && variant.choosesTarget
                    && field.getValue().equals("0") && otherValue != null && otherValue.matches("[1-9]\\d*");
            if (!targetReplacesThatObject && !choiceCarriesEventValue) {
                differences.add(field.getKey() + ": " + field.getValue() + " -> " + otherValue);
            }
        }
        return differences;
    }

    private Map<String, String> record(StackEntry entry) {
        Map<String, String> record = new LinkedHashMap<>();
        for (Field field : RECORD_FIELDS) {
            try {
                record.put(field.getName(), normalize(field.getName(), field.get(entry), entry));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        record.put("nonTargeting", String.valueOf(entry.isNonTargeting()));
        return record;
    }

    /** A record value by role, so the two entries compare equal when they record the same event. */
    private String normalize(String fieldName, Object value, StackEntry entry) {
        if (fieldName.endsWith("EntryVersion")) {
            return value == null || (value instanceof Number number && number.longValue() == 0) ? "0" : "set";
        }
        return normalize(value, entry);
    }

    private String normalize(Object value, StackEntry entry) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Permanent permanent) {
            boolean own = entry.getCard() != null && permanent.getOriginalCard() != null
                    && (permanent.getOriginalCard().getId().equals(entry.getCard().getId())
                    || permanent.getCard().getId().equals(entry.getCard().getId()));
            return "P(" + (own ? "SOURCE" : id(permanent.getId(), entry)) + ")";
        }
        if (value instanceof StackEntry) {
            return "S";
        }
        if (value instanceof Card card) {
            if (entry.getCard() != null && card.getId().equals(entry.getCard().getId())) {
                return "C(SELF)";
            }
            return "C(" + (fixtureNames.contains(card.getName()) ? "FIXTURE" : card.getName()) + ")";
        }
        if (value instanceof UUID uuid) {
            return id(uuid, entry);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(element -> normalize(element, entry)).sorted().toList().toString();
        }
        if (value instanceof Map<?, ?> map) {
            return "M" + map.size();
        }
        if (value instanceof String) {
            return "str";
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return value.toString();
        }
        return value.getClass().getSimpleName();
    }

    private String id(UUID uuid, StackEntry entry) {
        if (uuid == null) return "null";
        if (uuid.equals(entry.getSourcePermanentId())) return "SOURCE";
        if (entry.getCard() != null && entry.getCard().getId().equals(cardOfPermanent.get(uuid))) return "SOURCE";
        if (entry.getCard() != null && uuid.equals(entry.getCard().getId())) return "SELFCARD";
        if (uuid.equals(player1.getId())) return "p1";
        if (uuid.equals(player2.getId())) return "p2";
        if (firstTarget != null && uuid.equals(firstTarget.getId())) return "firstTarget";
        if (secondTarget != null && uuid.equals(secondTarget.getId())) return "secondTarget";
        return "id";
    }
}
