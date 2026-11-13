package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.BendingType;
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
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.VotingResult;
import com.github.laxika.magicalvibes.model.Zone;
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
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TransformChosenPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.DayNightService;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.service.effect.normalfx.UnattachTriggerSupport;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveTimeCounterFromExiledCardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TrainEffectHandler;
import com.github.laxika.magicalvibes.model.effect.TrainEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.ClassLevelUpEffectHandler;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EvolveEffectHandler;
import com.github.laxika.magicalvibes.model.effect.EvolveEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GoadSourceCreatureEffect;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
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
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
 * by role (the source, a player, a target), not by raw id. One difference is allowed, for a variant that chooses a
 * target: {@code nonTargeting} going from true to false, as the chosen target replaces the plain ability's
 * non-targeting "that player" or "that creature". The X of the two entries is the same: a target restricted by the
 * event ("that much", "that spell's mana value") is chosen against the record of the event, not against an X of the
 * choice.
 *
 * <p>Measuring mode: set the environment variable {@value #MEASURE_VARIABLE} to a file path and each case appends one
 * line to that file instead of asserting, {@code <watch> / <variant>: SAME}, {@code <watch> / <variant>: [field: plain
 * -> variant, ...]} or {@code <watch> / <variant>: NO ENTRY (plain=<n>, variant=<m>) <what the game is waiting for>},
 * so the lines can be counted before and after an engine change. Pairs listed in {@link #EXCLUDED} are skipped when
 * asserting and still measured.
 *
 * <p>Adding a watch is one entry in one of the lists {@link #watches()} joins, named after the {@link Triggers}
 * method it measures. Every such method has a watch or a reason in {@link #UNMEASURED}
 * ({@link #everyTriggerFactoryHasAWatchOrAReason}).
 */
@CardUsed({GrizzlyBears.class, Forest.class, Shock.class})
class TriggerRouteRecordTest extends BaseCardTest {

    private static final String MEASURE_VARIABLE = "MV_ROUTE_PROBE_OUT";

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
    private static final Map<String, String> EXCLUDED = excluded();

    private static Map<String, String> excluded() {
        Map<String, String> excluded = new HashMap<>();
        excluded.put("a token is created / GRAVE", "the token collector has no graveyard-card target route (no card "
                + "prints one): the ability is removed for want of a permanent target");
        String noGraveyardRoute = "the discard and cycling collector has no graveyard-card target route (no card "
                + "prints one): the ability isn't put on the stack";
        excluded.put("you discard a card / GRAVE", noGraveyardRoute);
        excluded.put("an opponent discards a card / GRAVE", noGraveyardRoute);
        excluded.put("a player cycles a card / GRAVE", noGraveyardRoute);
        for (Variant targeted : List.of(Variant.TARGET, Variant.TWO_GROUPS, Variant.GRAVE)) {
            excluded.put("a creature you control crews a Vehicle / " + targeted, "the crew collector refuses an "
                    + "ability that chooses a target (IllegalStateException): it has one route");
        }
        for (Variant targeted : List.of(Variant.TARGET, Variant.TWO_GROUPS, Variant.GRAVE)) {
            excluded.put("this enlists a creature / " + targeted, "the enlist collector refuses an ability that "
                    + "chooses a target (IllegalStateException): it has one route");
        }
        for (Variant targeted : List.of(Variant.TARGET, Variant.TWO_GROUPS)) {
            excluded.put("a creature deals damage to enchanted planeswalker / " + targeted, "a fixture Aura whose "
                    + "target group is a creature can't stay attached to a planeswalker: it is put into the "
                    + "graveyard before the damage is dealt");
        }
        excluded.put("this champions a creature / GRAVE", "the championed ability's target choice offers players "
                + "and permanents only (no card prints a graveyard card): the ability is removed");
        excluded.put("a creature you control becomes the target of an opponent's spell or ability / GRAVE",
                "the collector's target choice offers permanents only (no card prints a graveyard card): the "
                        + "ability is removed");
        excluded.put("another nontoken artifact you control is put into a graveyard or exile / GRAVE",
                "the collector's target choice offers permanents and players only (no card prints a graveyard "
                        + "card): the ability is removed");
        excluded.put("a player draws a card / GRAVE", "the emblem draw collector refuses an ability with a target "
                + "that isn't a permanent (IllegalStateException)");
        // A target the variant chooses as it goes on the stack is the watched event again (CR 603.3d through
        // CR 601.2c): the plain ability and the variant trigger for each target the variant chooses, without end.
        String targetRetriggers = "the variant's own target is the watched event again: both abilities trigger "
                + "for each target it chooses, without end";
        for (String watch : List.of("a creature becomes the target of a spell or ability",
                "a creature an opponent controls becomes the target of your spell or ability",
                "a player chooses targets", "a player or permanent becomes the target of your ability")) {
            for (Variant targeted : List.of(Variant.TARGET, Variant.TWO_GROUPS, Variant.MODAL_TARGET)) {
                excluded.put(watch + " / " + targeted, targetRetriggers);
            }
        }
        excluded.put("a player chooses targets / " + Variant.GRAVE, targetRetriggers);
        return Map.copyOf(excluded);
    }


    /**
     * The {@link Triggers} methods no watch measures, each with the specific reason a fixture card on that trigger
     * can't be compared route by route.
     */
    private static final Map<String, String> UNMEASURED = Map.of(
            "creatureBecomesTapped", "read only for a temporary ability registered until a player's next turn "
                    + "(TriggerCollectionService.collectTemporaryGlobalTriggers; Don't Move): no collector reads it "
                    + "on a card, so a fixture never triggers, and the temporary ability has one route",
            "playerGainsLife", "read only for a temporary ability registered until end of turn "
                    + "(TriggerCollectionService.collectTemporaryGlobalTriggers; False Cure): no collector reads it "
                    + "on a card, so a fixture never triggers, and the temporary ability has one route");

    /**
     * "This ability triggers only once each turn", for a watch whose targeted fixture would make its own event happen
     * again: targeting an opponent's creature is a crime.
     */
    private static final TriggerOptions ONCE_EACH_TURN = TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN);

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
    record Watch(String name, String factory, Supplier<Trigger> trigger, TriggerOptions options, CardType host,
                 List<CardSubtype> subtypes, BiConsumer<TriggerRouteRecordTest, List<Card>> scenario,
                 AbilityZones zones) {

        /** The same watch with its ability written with {@code options}, as "one or more" abilities are. */
        Watch with(TriggerOptions options) {
            return new Watch(name, factory, trigger, options, host, subtypes, scenario, zones);
        }

        /** The same watch with its ability functioning in {@code zones} instead of where its trigger does. */
        Watch in(AbilityZones zones) {
            return new Watch(name, factory, trigger, options, host, subtypes, scenario, zones);
        }

        void addAbility(Card card, CardEffect... effects) {
            if (zones == null) {
                card.addTrigger(trigger.get(), options, effects);
            } else {
                card.addTrigger(new TriggerCondition(trigger.get(), zones), options, effects);
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** A watch on the trigger the {@code Triggers} method {@code factory} makes. */
    private static Watch watch(String name, String factory, Supplier<Trigger> trigger, CardType host,
                               BiConsumer<TriggerRouteRecordTest, List<Card>> scenario, CardSubtype... subtypes) {
        return new Watch(name, factory, trigger, TriggerOptions.DEFAULT, host, List.of(subtypes), scenario, null);
    }

    static List<Watch> watches() {
        return Stream.of(eventWatches(), furtherWatches(), playerActionWatches(), libraryAndTapWatches(),
                        objectAndSpellWatches(), objectActionWatches(), counterWatches(),
                        targetAndDrawWatches(), zoneAndStepWatches(), combatAndDamageWatches())
                .flatMap(List::stream).toList();
    }

    private static List<Watch> eventWatches() {
        return List.of(
                watch("self attack", "attacks", Triggers::attacks, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAttack),
                watch("equipment attack", "attacks", Triggers::attacks, CardType.ARTIFACT,
                        TriggerRouteRecordTest::equippedCreatureAttacks, CardSubtype.EQUIPMENT),
                watch("you attack", "youAttack", Triggers::youAttack, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("a creature you control attacks", "creatureYouControlAttacks", Triggers::creatureYouControlAttacks, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("attacks and isn't blocked", "attacksUnblocked", Triggers::attacksUnblocked, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAttackUnblocked),
                watch("this blocks", "blocks", Triggers::blocks, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBlock),
                watch("this becomes blocked", "becomesBlocked", Triggers::becomesBlocked, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBecomeBlocked),
                watch("deals combat damage to a player", "dealsCombatDamageToPlayer", Triggers::dealsCombatDamageToPlayer, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("equipped creature deals combat damage to a player", "dealsCombatDamageToPlayer", Triggers::dealsCombatDamageToPlayer,
                        CardType.ARTIFACT, TriggerRouteRecordTest::equippedCreatureDealsCombatDamageToPlayer,
                        CardSubtype.EQUIPMENT),
                watch("own enters", "enters", () -> Triggers.enters(ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesEnter),
                watch("own enters, cast with its costs paid", "enters", () -> Triggers.enters(ObjectRelation.THIS),
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesEnterWithCostsPaid),
                watch("a creature enters", "enters", () -> Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureEnters),
                watch("own death", "dies", () -> Triggers.dies(ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDestroyed),
                watch("a creature dies", "dies", () -> Triggers.dies(Triggers.CREATURE, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsDestroyed),
                watch("your upkeep", "upkeep", () -> Triggers.upkeep(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::upkeepBegins),
                watch("you cast a spell", "youCastSpell", () -> Triggers.youCastSpell().anySpell(), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youCastACreatureSpell),
                watch("you gain life", "youGainLife", Triggers::youGainLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new GainLifeEffect(3))),
                watch("your end step", "endStep", () -> Triggers.endStep(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.POSTCOMBAT_MAIN, TurnStep.END_STEP)),
                watch("your draw step", "drawStep", () -> Triggers.drawStep(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.UPKEEP, TurnStep.DRAW)),
                watch("beginning of combat", "beginningOfCombat", () -> Triggers.beginningOfCombat(ActivePlayer.YOU),
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.PRECOMBAT_MAIN,
                                TurnStep.BEGINNING_OF_COMBAT)),
                watch("you lose life", "youLoseLife", Triggers::youLoseLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER))),
                watch("an opponent gains life", "opponentGainsLife", Triggers::opponentGainsLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new GainLifeEffect(new Fixed(1), GainLifeRecipient.OPPONENT))),
                watch("an opponent loses life", "opponentLosesLife", Triggers::opponentLosesLife, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT))),
                watch("you draw a card", "youDrawCard", Triggers::youDrawCard, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new DrawCardEffect(1))),
                watch("you discard a card", "youDiscardCard", Triggers::youDiscardCard, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER, true))),
                watch("counters put on this", "countersPutOnThis", Triggers::countersPutOnThis, CardType.CREATURE,
                        TriggerRouteRecordTest::countersArePutOnFixtures),
                watch("counters put on a creature you control", "countersPutOnCreatureYouControl", Triggers::countersPutOnCreatureYouControl,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("becomes tapped", "becomesTapped", Triggers::becomesTapped, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAttack),
                watch("becomes untapped", "becomesUntapped", Triggers::becomesUntapped, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesUntap),
                watch("a permanent you control becomes tapped", "permanentYouControlBecomesTapped", Triggers::permanentYouControlBecomesTapped,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("own sacrifice", "sacrificed", () -> Triggers.sacrificed(ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreSacrificed),
                watch("you sacrifice a permanent", "sacrificed", () -> Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsSacrificed),
                watch("own leaves the battlefield", "leavesBattlefield", () -> Triggers.leavesBattlefield(ObjectRelation.THIS),
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesAreDestroyed),
                watch("a creature leaves the battlefield", "leavesBattlefield",
                        () -> Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsDestroyed),
                watch("becomes the target of a spell or ability", "becomesTargetOfSpellOrAbility", Triggers::becomesTargetOfSpellOrAbility,
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesBecomeSpellTargets),
                watch("a creature becomes the target of a spell or ability", "creatureBecomesTargetOfSpellOrAbility",
                        Triggers::creatureBecomesTargetOfSpellOrAbility, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBecomesSpellTarget),
                watch("is dealt damage", "dealtDamage", Triggers::dealtDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDealtCombatDamage),
                watch("deals damage", "dealsDamage", Triggers::dealsDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("you are dealt damage", "youAreDealtDamage", Triggers::youAreDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))),
                watch("an opponent is dealt damage", "opponentDealtDamage", Triggers::opponentDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))),
                watch("an opponent casts a spell", "opponentCastsSpell", () -> Triggers.opponentCastsSpell().anySpell(),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::opponentCastsACreatureSpell),
                watch("a land enters under your control", "enters",
                        () -> Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::landEnters),
                watch("you play a land", "youPlayLand", Triggers::youPlayLand, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youPlayALand),
                watch("a creature blocks", "creatureBlocks", Triggers::creatureBlocks, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBlocks),
                watch("a creature you control becomes blocked", "creatureYouControlBecomesBlocked", Triggers::creatureYouControlBecomesBlocked,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureBecomesBlocked),
                watch("a creature you control deals combat damage to a player", "creatureYouControlDealsCombatDamageToPlayer",
                        Triggers::creatureYouControlDealsCombatDamageToPlayer, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("you tap a land for mana", "youTapLandForMana", Triggers::youTapLandForMana, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youTapALandForMana),
                watch("you scry", "youScry", Triggers::youScry, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperLooksAtLibrary(cards, new ScryEffect(1))),
                watch("you surveil", "youSurveil", Triggers::youSurveil, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperLooksAtLibrary(cards, new SurveilEffect(1))),
                watch("a token is created", "tokensCreated",
                        () -> Triggers.tokensCreated(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new CreateTokenEffect(
                                1, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), false)))
                        .with(TriggerOptions.ONE_OR_MORE),
                watch("turned face up", "turnedFaceUp", () -> Triggers.turnedFaceUp(null, ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreTurnedFaceUp),
                watch("transforms into its back face", "transformsToBackFace", () -> Triggers.transformsToBackFace(ObjectRelation.THIS),
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesTransform),
                watch("chaos ensues", "chaosEnsues", Triggers::chaosEnsues, CardType.PLANE,
                        TriggerRouteRecordTest::chaosEnsues),
                watch("day becomes night", "dayBecomesNightOrNightBecomesDay", Triggers::dayBecomesNightOrNightBecomesDay, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::dayBecomesNight),
                watch("you roll a die", "youRollDie", Triggers::youRollDie, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new RollD6Effect(new Fixed(1), List.of()))));
    }

    /**
     * The watches run 17z17 added: the damage, counter, tap, zone-change, combat and step collectors the first list
     * reaches through one trigger only, each on another of its triggers.
     */
    private static List<Watch> furtherWatches() {
        return List.of(
                watch("a creature attacks", "creatureAttacks", Triggers::creatureAttacks, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("a player attacks", "playerAttacks", Triggers::playerAttacks, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("you attack a player", "youAttackPlayer", Triggers::youAttackPlayer, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("a creature attacks you", "creatureAttacksYou", Triggers::creatureAttacksYou, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureAttacks),
                watch("creatures attack you", "creaturesAttackYou", Triggers::creaturesAttackYou, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureAttacks),
                watch("a creature you control attacks and isn't blocked", "creatureYouControlAttacksUnblocked", Triggers::creatureYouControlAttacksUnblocked,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureAttacksUnblocked),
                watch("creatures block", "creaturesBlock", Triggers::creaturesBlock, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBlocks),
                watch("a creature becomes blocked", "creatureBecomesBlocked", Triggers::creatureBecomesBlocked, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBecomesBlocked),
                watch("creatures you control become blocked", "creaturesYouControlBecomeBlocked", Triggers::creaturesYouControlBecomeBlocked,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureBecomesBlocked),
                watch("deals combat damage", "dealsCombatDamage", Triggers::dealsCombatDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("deals combat damage to a creature", "dealsCombatDamageToCreature", Triggers::dealsCombatDamageToCreature, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDealtCombatDamage),
                watch("is dealt combat damage", "dealtCombatDamage", Triggers::dealtCombatDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDealtCombatDamage),
                watch("deals damage to a creature", "dealsDamageToCreature", Triggers::dealsDamageToCreature, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDealtCombatDamage),
                watch("deals damage to a player", "dealsDamageToPlayer", Triggers::dealsDamageToPlayer, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("deals damage to an opponent", "dealsDamageToOpponent", Triggers::dealsDamageToOpponent, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("deals combat damage to a player or planeswalker", "dealsCombatDamageToPlayerOrPlaneswalker",
                        Triggers::dealsCombatDamageToPlayerOrPlaneswalker, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("equipped creature deals combat damage", "equippedCreatureDealsCombatDamage", Triggers::equippedCreatureDealsCombatDamage,
                        CardType.ARTIFACT, TriggerRouteRecordTest::equippedCreatureDealsCombatDamageToPlayer,
                        CardSubtype.EQUIPMENT),
                watch("equipped creature deals combat damage to a player, as the Equipment's trigger", "equippedCreatureDealsCombatDamageToPlayer",
                        Triggers::equippedCreatureDealsCombatDamageToPlayer, CardType.ARTIFACT,
                        TriggerRouteRecordTest::equippedCreatureDealsCombatDamageToPlayer, CardSubtype.EQUIPMENT),
                watch("a creature deals combat damage to a player", "creatureDealsCombatDamageToPlayer", Triggers::creatureDealsCombatDamageToPlayer,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a creature you control deals combat damage", "creatureYouControlDealsCombatDamage", Triggers::creatureYouControlDealsCombatDamage,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a creature deals combat damage to an opponent", "creatureDealsCombatDamageToOpponent", Triggers::creatureDealsCombatDamageToOpponent,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("creatures you control deal damage to a player", "creaturesYouControlDealDamageToPlayer", Triggers::creaturesYouControlDealDamageToPlayer,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a source you control deals damage to an opponent", "sourceYouControlDealsDamageToOpponent",
                        Triggers::sourceYouControlDealsDamageToOpponent, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a creature you control deals damage to a creature", "creatureYouControlDealsDamageToCreature",
                        Triggers::creatureYouControlDealsDamageToCreature, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToCreature),
                watch("a creature deals damage to a creature", "creatureDealsDamageToCreature", Triggers::creatureDealsDamageToCreature,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToAWall),
                watch("you are dealt combat damage", "youAreDealtCombatDamage", Triggers::youAreDealtCombatDamage, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureDealsCombatDamageToYou),
                watch("a creature deals combat damage to you", "creatureDealsCombatDamageToYou", Triggers::creatureDealsCombatDamageToYou,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::opponentCreatureDealsCombatDamageToYou),
                watch("a source deals damage to you", "sourceDealsDamageToYou", Triggers::sourceDealsDamageToYou, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureDealsCombatDamageToYou),
                watch("you are dealt damage by a source you control", "youAreDealtDamageBySourceYouControl", Triggers::youAreDealtDamageBySourceYouControl,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))),
                watch("a player is dealt damage", "playerDealtDamage", Triggers::playerDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))),
                watch("an opponent is dealt noncombat damage", "opponentDealtNoncombatDamage", Triggers::opponentDealtNoncombatDamage,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))),
                watch("a source you control deals noncombat damage to an opponent", "sourceYouControlDealsNoncombatDamageToOpponent",
                        Triggers::sourceYouControlDealsNoncombatDamageToOpponent, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))),
                watch("a source you control deals damage", "sourceYouControlDealsDamage", Triggers::sourceYouControlDealsDamage,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))),
                watch("is dealt noncombat damage", "dealtNoncombatDamage", Triggers::dealtNoncombatDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDealtNoncombatDamage),
                watch("is dealt damage, not in combat", "dealtDamage", Triggers::dealtDamage, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDealtNoncombatDamage),
                watch("a creature is dealt damage", "creatureDealtDamage", Triggers::creatureDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentVictimIsDealtDamage(cards, 1)),
                watch("a permanent you control is dealt damage", "permanentYouControlDealtDamage", Triggers::permanentYouControlDealtDamage,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new MassDamageEffect(1))),
                watch("a creature an opponent controls is dealt damage", "creatureOpponentControlsDealtDamage",
                        Triggers::creatureOpponentControlsDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentVictimIsDealtDamage(cards, 1)),
                watch("a source you control deals noncombat damage to a creature", "sourceYouControlDealsNoncombatDamageToCreature",
                        Triggers::sourceYouControlDealsNoncombatDamageToCreature, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentVictimIsDealtDamage(cards, 1)),
                watch("a creature an opponent controls is dealt excess damage", "creatureOrPlaneswalkerOpponentControlsDealtExcessDamage",
                        Triggers::creatureOrPlaneswalkerOpponentControlsDealtExcessDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentVictimIsDealtDamage(cards, 6)),
                watch("a creature an opponent controls is dealt excess noncombat damage", "creatureOrPlaneswalkerOpponentControlsDealtExcessNoncombatDamage",
                        Triggers::creatureOrPlaneswalkerOpponentControlsDealtExcessNoncombatDamage,
                        CardType.ENCHANTMENT, (test, cards) -> test.opponentVictimIsDealtDamage(cards, 6)),
                watch("creatures your opponents control are dealt excess noncombat damage", "creaturesOpponentsControlDealtExcessNoncombatDamage",
                        Triggers::creaturesOpponentsControlDealtExcessNoncombatDamage, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentVictimIsDealtDamage(cards, 6)),
                watch("damage to you is prevented", "damageToYouPrevented", Triggers::damageToYouPrevented, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::damageToYouIsPrevented),
                watch("you draw your second card each turn", "youDrawSecondCardEachTurn", Triggers::youDrawSecondCardEachTurn,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new DrawCardEffect(2))),
                watch("you discard one or more cards", "youDiscardOneOrMoreCards", Triggers::youDiscardOneOrMoreCards, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER, true))),
                watch("a library is shuffled", "playerShufflesLibrary", Triggers::playerShufflesLibrary, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new ShuffleLibraryEffect(false))),
                watch("you proliferate", "youProliferate", Triggers::youProliferate, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new ProliferateEffect())),
                watch("+1/+1 counters put on this", "plusOneCountersPutOnThis", Triggers::plusOneCountersPutOnThis, CardType.CREATURE,
                        TriggerRouteRecordTest::countersArePutOnFixtures),
                watch("a +1/+1 counter put on this", "plusOneCounterPutOnThis", Triggers::plusOneCounterPutOnThis, CardType.CREATURE,
                        TriggerRouteRecordTest::countersArePutOnFixtures),
                watch("you put counters on a creature", "youPutCountersOnCreature", Triggers::youPutCountersOnCreature, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("you put counters on a permanent or player", "youPutCountersOnPermanentOrPlayer", Triggers::youPutCountersOnPermanentOrPlayer,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("a permanent you control becomes untapped", "permanentYouControlBecomesUntapped", Triggers::permanentYouControlBecomesUntapped,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherPermanentUntaps),
                watch("a permanent becomes untapped", "permanentBecomesUntapped", Triggers::permanentBecomesUntapped, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherPermanentUntaps),
                watch("a player taps a land for mana", "playerTapsLandForMana", Triggers::playerTapsLandForMana, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youTapALandForMana),
                watch("a creature you control dies", "creatureDies", () -> Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::yourCreatureIsDestroyed),
                watch("a nontoken creature you control dies", "nontokenCreatureDies",
                        () -> Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::yourCreatureIsDestroyed),
                watch("a creature dies without being sacrificed", "diesWithoutBeingSacrificed",
                        () -> Triggers.diesWithoutBeingSacrificed(Triggers.CREATURE, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsDestroyed),
                watch("a creature is put into a graveyard from the battlefield", "putIntoGraveyardFrom",
                        () -> Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.CREATURE, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsDestroyed),
                watch("own trip to the graveyard from anywhere", "putIntoGraveyardFromAnywhere",
                        () -> Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreDestroyed),
                watch("a creature enters under your control", "enters",
                        () -> Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureEnters),
                watch("another creature enters", "enters", () -> Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureEnters),
                watch("a creature enters under an opponent's control", "enters",
                        () -> Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::opponentCreatureEnters),
                watch("a player casts a spell", "playerCastsSpell", () -> Triggers.playerCastsSpell().anySpell(), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youCastACreatureSpell),
                watch("becomes the target of a spell", "becomesTargetOfSpell", Triggers::becomesTargetOfSpell, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBecomeSpellTargets),
                watch("becomes the target of your spell", "becomesTargetOfYourSpell", Triggers::becomesTargetOfYourSpell, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBecomeSpellTargets),
                watch("a creature you control becomes the target of a spell", "creatureYouControlBecomesTargetOfSpell",
                        Triggers::creatureYouControlBecomesTargetOfSpell, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBecomesSpellTarget),
                watch("another creature you control becomes the target of a spell or ability", "anotherCreatureYouControlBecomesTargetOfSpellOrAbility",
                        Triggers::anotherCreatureYouControlBecomesTargetOfSpellOrAbility, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBecomesSpellTarget),
                watch("each upkeep", "upkeep", () -> Triggers.upkeep(ActivePlayer.ANY), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::upkeepBegins),
                watch("each end step", "endStep", () -> Triggers.endStep(ActivePlayer.ANY), CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.POSTCOMBAT_MAIN, TurnStep.END_STEP)),
                watch("your precombat main phase", "precombatMainPhase", () -> Triggers.precombatMainPhase(ActivePlayer.YOU),
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.DRAW, TurnStep.PRECOMBAT_MAIN)),
                watch("aura attack", "attacks", Triggers::attacks, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::equippedCreatureAttacks, CardSubtype.AURA),
                watch("enchanted creature deals combat damage to a player", "dealsCombatDamageToPlayer", Triggers::dealsCombatDamageToPlayer,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::equippedCreatureDealsCombatDamageToPlayer,
                        CardSubtype.AURA),
                watch("enchanted creature deals damage", "enchantedCreatureDealsDamage", Triggers::enchantedCreatureDealsDamage,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::equippedCreatureDealsCombatDamageToPlayer,
                        CardSubtype.AURA),
                watch("enchanted creature is dealt damage", "enchantedCreatureDealtDamage", Triggers::enchantedCreatureDealtDamage,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::enchantedCreatureIsDealtCombatDamage,
                        CardSubtype.AURA),
                watch("enchanted creature attacks and isn't blocked", "enchantedCreatureAttacksUnblocked", Triggers::enchantedCreatureAttacksUnblocked,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::enchantedCreatureAttacksUnblocked,
                        CardSubtype.AURA),
                watch("enchanted permanent becomes tapped", "enchantedPermanentBecomesTapped", Triggers::enchantedPermanentBecomesTapped,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::equippedCreatureAttacks, CardSubtype.AURA),
                watch("a creature attacks you or your planeswalker", "creatureAttacksYouOrYourPlaneswalker", Triggers::creatureAttacksYouOrYourPlaneswalker,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::opponentCreatureAttacks),
                watch("a creature attacks while you defend", "creatureAttacksWhileYouDefend", Triggers::creatureAttacksWhileYouDefend,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::opponentCreatureAttacks),
                watch("you attack a player or planeswalker", "youAttackPlayerOrPlaneswalker", Triggers::youAttackPlayerOrPlaneswalker,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("deals combat damage to a blocking creature", "dealsCombatDamageToBlockingCreature", Triggers::dealsCombatDamageToBlockingCreature,
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesAreDealtCombatDamage),
                watch("deals combat damage to a player or battle", "dealsCombatDamageToPlayerOrBattle", Triggers::dealsCombatDamageToPlayerOrBattle,
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("a creature you control deals combat damage to a player or battle", "creatureYouControlDealsCombatDamageToPlayerOrBattle",
                        Triggers::creatureYouControlDealsCombatDamageToPlayerOrBattle, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("equipped creature deals combat damage to a player or battle", "equippedCreatureDealsCombatDamageToPlayerOrBattle",
                        Triggers::equippedCreatureDealsCombatDamageToPlayerOrBattle, CardType.ARTIFACT,
                        TriggerRouteRecordTest::equippedCreatureDealsCombatDamageToPlayer, CardSubtype.EQUIPMENT),
                watch("creatures you control deal damage to an opponent", "creaturesYouControlDealDamageToOpponent",
                        Triggers::creaturesYouControlDealDamageToOpponent, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a source you control deals damage to a player", "sourceYouControlDealsDamageToPlayer", Triggers::sourceYouControlDealsDamageToPlayer,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a source deals damage", "sourceDealsDamage", Triggers::sourceDealsDamage, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a source deals damage to a permanent or player", "sourceDealsDamageToPermanentOrPlayer",
                        Triggers::sourceDealsDamageToPermanentOrPlayer, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("another source you control deals damage to a permanent or player", "anotherSourceYouControlDealsDamageToPermanentOrPlayer",
                        Triggers::anotherSourceYouControlDealsDamageToPermanentOrPlayer, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureDealsCombatDamageToPlayer),
                watch("a permanent deals damage to you", "permanentDealsDamageToYou", Triggers::permanentDealsDamageToYou, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureDealsCombatDamageToYou),
                watch("a creature deals damage to you or your permanent", "creatureDealsDamageToYouOrYourPermanent",
                        Triggers::creatureDealsDamageToYouOrYourPermanent, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureDealsCombatDamageToYou),
                watch("you are dealt damage by a source an opponent controls", "youAreDealtDamageBySourceOpponentControls",
                        Triggers::youAreDealtDamageBySourceOpponentControls, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureDealsCombatDamageToYou),
                watch("a source an opponent controls deals damage to you or your permanent", "sourceOpponentControlsDealsDamageToYouOrYourPermanent",
                        Triggers::sourceOpponentControlsDealsDamageToYouOrYourPermanent, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentCreatureDealsCombatDamageToYou),
                watch("you put counters on a creature you control", "youPutCountersOnCreatureYouControl", Triggers::youPutCountersOnCreatureYouControl,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("+1/+1 counters put on another creature you control", "plusOneCountersPutOnAnotherCreatureYouControl",
                        Triggers::plusOneCountersPutOnAnotherCreatureYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("you put counters on another creature", "youPutCountersOnAnotherCreature", Triggers::youPutCountersOnAnotherCreature,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("you put +1/+1 counters on this", "youPutPlusOneCountersOnThis", Triggers::youPutPlusOneCountersOnThis, CardType.CREATURE,
                        TriggerRouteRecordTest::countersArePutOnFixtures),
                watch("becomes tapped for the first time during your turn", "becomesTappedForFirstTimeDuringYourTurn",
                        Triggers::becomesTappedForFirstTimeDuringYourTurn, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAttack),
                watch("a creature you control becomes tapped for the first time during your turn", "creatureYouControlBecomesTappedForFirstTimeDuringYourTurn",
                        Triggers::creatureYouControlBecomesTappedForFirstTimeDuringYourTurn, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("a creature is sacrificed", "sacrificed", () -> Triggers.sacrificed(Triggers.CREATURE, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureIsSacrificed),
                watch("another creature you control dies", "creatureDies",
                        () -> Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::yourCreatureIsDestroyed),
                watch("another permanent enters under your control", "enters",
                        () -> Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureEnters),
                watch("a land enters", "enters", () -> Triggers.enters(Triggers.LAND, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::landEnters),
                watch("an opponent's upkeep", "upkeep", () -> Triggers.upkeep(ActivePlayer.OPPONENT), CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentUpkeepBegins),
                watch("each draw step", "drawStep", () -> Triggers.drawStep(ActivePlayer.ANY), CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.UPKEEP, TurnStep.DRAW)),
                watch("each beginning of combat", "beginningOfCombat", () -> Triggers.beginningOfCombat(ActivePlayer.ANY),
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.PRECOMBAT_MAIN,
                                TurnStep.BEGINNING_OF_COMBAT)),
                watch("you draw your second card each turn, as the nth", "youDrawNthCardEachTurn", () -> Triggers.youDrawNthCardEachTurn(2),
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new DrawCardEffect(2))));
    }

    /**
     * The player-action triggers: each is collected by one {@link TriggerCollectionService} method the engine calls
     * as the action is taken, which the scenario calls for player1 with the fixtures on the battlefield.
     */
    private static List<Watch> playerActionWatches() {
        return List.of(
                watch("you untap permanents during your untap step", "youUntapPermanentsDuringUntapStep",
                        Triggers::youUntapPermanentsDuringUntapStep, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::untapStepFinishes),
                collected("you set a scheme in motion", "youSetSchemeInMotion", Triggers::youSetSchemeInMotion,
                        (test, collection) -> collection.checkSchemeSetInMotionTriggers(test.gd, new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY, card("Fixture Scheme", CardType.SCHEME),
                                test.player1.getId(), "Fixture Scheme's ability",
                                new ArrayList<>(List.of(new GainLifeEffect(1)))))),
                collected("you give a gift", "youGiveGift", Triggers::youGiveGift,
                        (test, collection) -> collection.checkControllerGivesGiftTriggers(test.gd,
                                test.player1.getId())),
                collected("you solve a Case", "youSolveCase", Triggers::youSolveCase,
                        (test, collection) -> collection.checkAllyCaseSolvesTriggers(test.gd,
                                test.addCreatureReady(test.player1, new GrizzlyBears()), test.player1.getId())),
                collected("you foretell a card", "youForetell", Triggers::youForetell,
                        (test, collection) -> collection.checkControllerForetellTriggers(test.gd,
                                test.player1.getId(), new GrizzlyBears())),
                collected("you forage", "youForage", Triggers::youForage,
                        (test, collection) -> collection.checkForageTriggers(test.gd, test.player1.getId())),
                collected("players finish voting", "playersFinishVoting", Triggers::playersFinishVoting,
                        (test, collection) -> collection.checkVotingFinishedTriggers(test.gd, new VotingResult(
                                Map.of(test.player1.getId(), Set.of("first"),
                                        test.player2.getId(), Set.of("second"))))),
                collected("the Ring tempts you and you choose a Ring-bearer", "ringTemptsYouAndYouChooseRingBearer",
                        Triggers::ringTemptsYouAndYouChooseRingBearer,
                        (test, collection) -> collection.checkRingTemptTriggers(test.gd, test.player1.getId(),
                                test.addCreatureReady(test.player1, new GrizzlyBears()).getId())),
                collected("you investigate for the first time each turn", "youInvestigateFirstTimeEachTurn",
                        Triggers::youInvestigateFirstTimeEachTurn,
                        (test, collection) -> collection.checkInvestigateTriggers(test.gd, test.player1.getId())),
                collected("you investigate", "youInvestigate", Triggers::youInvestigate,
                        (test, collection) -> collection.checkInvestigateTriggers(test.gd, test.player1.getId())),
                collected("you manifest dread", "youManifestDread", Triggers::youManifestDread,
                        (test, collection) -> collection.checkManifestDreadTriggers(test.gd, test.player1.getId(),
                                test.gd.playerGraveyards.get(test.player1.getId()).getFirst())),
                collected("you complete a dungeon", "youCompleteDungeon", Triggers::youCompleteDungeon,
                        (test, collection) -> collection.checkDungeonCompletionTriggers(test.gd,
                                test.player1.getId())),
                collected("you roll one or more dice", "youRollOneOrMoreDice", Triggers::youRollOneOrMoreDice,
                        (test, collection) -> collection.checkControllerRollsOneOrMoreDiceTriggers(test.gd,
                                test.player1.getId(), 1, 4)),
                collected("you roll the highest natural result", "youRollHighestNaturalResult",
                        Triggers::youRollHighestNaturalResult,
                        (test, collection) -> collection.checkControllerRollsHighestNaturalResultTriggers(test.gd,
                                test.player1.getId(), 6, 6)),
                watch("you roll a natural 20", "youRollNatural20", Triggers::youRollNatural20, CardType.CREATURE,
                        (test, cards) -> test.fixturesInGraveyard(cards, collection ->
                                collection.checkControllerRollsNaturalTwentyTriggers(test.gd, test.player1.getId())))
                        .in(AbilityZones.GRAVEYARD),
                collected("you bend", "youBend", Triggers::youBend,
                        (test, collection) -> collection.checkBendingTriggers(test.gd, test.player1.getId(),
                                BendingType.EARTHBEND)),
                collected("you exert a creature", "youExertCreature", Triggers::youExertCreature,
                        (test, collection) -> collection.checkExertTriggers(test.gd, test.player1.getId(),
                                test.addCreatureReady(test.player1, new GrizzlyBears()).getId())),
                collected("you collect evidence", "youCollectEvidence", Triggers::youCollectEvidence,
                        (test, collection) -> collection.checkCollectEvidenceTriggers(test.gd,
                                test.player1.getId())),
                collected("you discover", "youDiscover", Triggers::youDiscover,
                        (test, collection) -> collection.checkDiscoverTriggers(test.gd, test.player1.getId(), 3)),
                collected("you seek", "youSeek", Triggers::youSeek,
                        (test, collection) -> collection.checkSeekTriggers(test.gd, test.player1.getId(),
                                List.of(new GrizzlyBears()))),
                collected("you conjure", "youConjure", Triggers::youConjure,
                        (test, collection) -> collection.checkConjureTriggers(test.gd, test.player1.getId(), 1)),
                collected("you become the monarch", "youBecomeMonarch", Triggers::youBecomeMonarch,
                        (test, collection) -> {
                            test.gd.monarchPlayerId = test.player1.getId();
                            collection.checkBecomesMonarchTriggers(test.gd, test.player1.getId());
                        }),
                collected("an opponent becomes the monarch", "opponentBecomesMonarch",
                        Triggers::opponentBecomesMonarch,
                        (test, collection) -> {
                            test.gd.monarchPlayerId = test.player2.getId();
                            collection.checkBecomesMonarchTriggers(test.gd, test.player2.getId());
                        }),
                collected("the Ring tempts you", "ringTemptsYou", Triggers::ringTemptsYou,
                        (test, collection) -> collection.checkRingTemptsYouTriggers(test.gd, test.player1.getId())),
                collected("you win a coin flip", "youWinCoinFlip", Triggers::youWinCoinFlip,
                        (test, collection) -> collection.checkControllerWinsCoinFlipTriggers(test.gd,
                                test.player1.getId())),
                collected("a player wins a coin flip", "playerWinsCoinFlip", Triggers::playerWinsCoinFlip,
                        (test, collection) -> collection.checkControllerWinsCoinFlipTriggers(test.gd,
                                test.player2.getId())),
                collected("you lose a coin flip", "youLoseCoinFlip", Triggers::youLoseCoinFlip,
                        (test, collection) -> collection.checkControllerLosesCoinFlipTriggers(test.gd,
                                test.player1.getId())),
                collected("an opponent pays a tax", "opponentPaysTax", Triggers::opponentPaysTax,
                        (test, collection) -> collection.checkTaxPaymentTriggers(test.gd, test.player2.getId(),
                                test.player1.getId(), 2)),
                collected("you clash", "youClash", Triggers::youClash,
                        (test, collection) -> collection.performClash(test.gd, test.player1.getId())));
    }

    /** The draw, discard, cycling, scry, search, shuffle and tap-for-mana triggers the first lists don't have. */
    private static List<Watch> libraryAndTapWatches() {
        return List.of(
                watch("an opponent draws a card except the first in their draw step",
                        "opponentDrawsCardExceptFirstInDrawStep", Triggers::opponentDrawsCardExceptFirstInDrawStep,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new EachOpponentDrawsCardEffect(1))),
                watch("enchanted player draws a card", "enchantedPlayerDrawsCard", Triggers::enchantedPlayerDrawsCard,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::enchantedPlayerDraws,
                        CardSubtype.AURA, CardSubtype.CURSE),
                watch("you reveal a card this way", "youRevealCardThisWay", Triggers::youRevealCardThisWay,
                        CardType.ENCHANTMENT, (test, cards) -> {
                            cards.forEach(card -> card.addStaticEffect(new RevealDrawnCardsEffect(
                                    RevealDrawnCardsEffect.Revealer.CONTROLLER, false, false, false)));
                            test.helperAbilityResolves(cards, new DrawCardEffect(1));
                        }),
                watch("an opponent discards a card", "opponentDiscardsCard", Triggers::opponentDiscardsCard,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT, true))),
                watch("an opponent causes you to discard a card", "opponentCausesYouToDiscardCard",
                        Triggers::opponentCausesYouToDiscardCard, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.opponentHelperAbilityResolves(
                                    new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT, true));
                        }),
                watch("a player cycles a card", "playerCyclesCard", Triggers::playerCyclesCard, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youCycleACard),
                watch("you cycle a card", "youCycleCard", Triggers::youCycleCard, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youCycleACard),
                watch("you cycle this card", "youCycleThisCard", Triggers::youCycleThisCard, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreCycled),
                watch("you discard this card", "youDiscardThisCard", Triggers::youDiscardThisCard, CardType.CREATURE,
                        (test, cards) -> {
                            for (Card card : cards) {
                                test.harness.setHand(test.player1, List.of(card));
                                test.helperEntersAndItsAbilityResolves(
                                        new DiscardEffect(1, DiscardRecipient.CONTROLLER, true));
                                test.answerAll();
                            }
                        }),
                watch("an opponent causes you to discard this card", "opponentCausesYouToDiscardThisCard",
                        Triggers::opponentCausesYouToDiscardThisCard, CardType.CREATURE,
                        (test, cards) -> {
                            for (Card card : cards) {
                                test.harness.setHand(test.player1, List.of(card));
                                test.opponentHelperAbilityResolves(
                                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT, true));
                                test.answerAll();
                            }
                        }),
                watch("an opponent scries", "opponentScries", Triggers::opponentScries, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentLooksAtLibrary(cards, new ScryEffect(1), List.of(0))),
                watch("an opponent surveils", "opponentSurveils", Triggers::opponentSurveils, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentLooksAtLibrary(cards, new SurveilEffect(2), List.of(0, 1))),
                watch("you search your library", "youSearchYourLibrary", Triggers::youSearchYourLibrary,
                        CardType.ENCHANTMENT, (test, cards) -> test.librarySearched(cards, test.player1)),
                watch("an opponent searches their library", "opponentSearchesTheirLibrary",
                        Triggers::opponentSearchesTheirLibrary, CardType.ENCHANTMENT,
                        (test, cards) -> test.librarySearched(cards, test.player2)),
                watch("an opponent shuffles their library", "opponentShufflesLibrary",
                        Triggers::opponentShufflesLibrary, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.opponentHelperAbilityResolves(new ShuffleLibraryEffect(false));
                        }),
                watch("a spell or ability causes its controller to shuffle",
                        "spellOrAbilityCausesItsControllerToShuffle",
                        Triggers::spellOrAbilityCausesItsControllerToShuffle, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new ShuffleLibraryEffect(false))),
                watch("this is tapped for mana", "tappedForMana", Triggers::tappedForMana, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesAreTappedForMana),
                watch("an opponent taps a land for mana", "opponentTapsLandForMana",
                        Triggers::opponentTapsLandForMana, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentTapsALandForMana),
                watch("enchanted land is tapped for mana", "enchantedLandTappedForMana",
                        Triggers::enchantedLandTappedForMana, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::enchantedLandIsTappedForMana, CardSubtype.AURA),
                watch("you tap this land for mana", "youTapThisLandForMana", Triggers::youTapThisLandForMana,
                        CardType.LAND, TriggerRouteRecordTest::fixtureLandsAreTappedForMana),
                watch("a player taps a permanent for mana", "playerTapsPermanentForMana",
                        Triggers::playerTapsPermanentForMana, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youTapALandForMana),
                watch("an opponent taps a permanent for mana", "opponentTapsPermanentForMana",
                        Triggers::opponentTapsPermanentForMana, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentTapsALandForMana),
                watch("you tap a creature for mana", "youTapCreatureForMana", Triggers::youTapCreatureForMana,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.youTapForMana(cards, card("Dork", CardType.CREATURE))),
                watch("you tap a nonland permanent for mana", "youTapNonlandPermanentForMana",
                        Triggers::youTapNonlandPermanentForMana, CardType.ENCHANTMENT,
                        (test, cards) -> test.youTapForMana(cards, card("Dork", CardType.CREATURE))),
                watch("you tap an artifact token for mana", "youTapArtifactTokenForMana",
                        Triggers::youTapArtifactTokenForMana, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            Card rock = card("Rock", CardType.ARTIFACT);
                            rock.setToken(true);
                            test.youTapForMana(cards, rock);
                        }),
                watch("a permanent an opponent controls becomes tapped", "permanentOpponentControlsBecomesTapped",
                        Triggers::permanentOpponentControlsBecomesTapped, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youTapAnOpponentCreature),
                watch("you tap a permanent an opponent controls", "youTapPermanentOpponentControls",
                        Triggers::youTapPermanentOpponentControls, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::youTapAnOpponentCreature),
                watch("this is tapped to pay a teamwork cost", "tappedToPayTeamworkCost",
                        Triggers::tappedToPayTeamworkCost, CardType.CREATURE,
                        (test, cards) -> {
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                permanent.tap();
                                test.collect(collection ->
                                        collection.checkSelfTappedToPayTeamworkCostTriggers(test.gd, permanent));
                                test.answerAll();
                            }
                        }));
    }

    /**
     * The Saga, control, attachment, planar, state, spell and activation triggers the first lists don't have.
     */
    private static List<Watch> objectAndSpellWatches() {
        return List.of(
                collected("a final chapter ability triggers", "finalChapterAbilityTriggers",
                        Triggers::finalChapterAbilityTriggers,
                        (test, collection) -> collection.checkSagaFinalChapterAbilityTriggers(test.gd,
                                test.player1.getId(), 3)),
                collected("a final chapter ability resolves", "finalChapterAbilityResolves",
                        Triggers::finalChapterAbilityResolves,
                        (test, collection) -> collection.checkSagaFinalChapterAbilityResolutionTriggers(test.gd,
                                test.player1.getId(), 3)),
                watch("you gain control of this", "youGainControlOfThis", Triggers::youGainControlOfThis,
                        CardType.CREATURE, (test, cards) -> {
                            for (Card card : cards) {
                                test.changeControl(test.addCreatureReady(test.player2, card), test.player1);
                                test.answerAll();
                            }
                        }),
                watch("you lose control of this", "youLoseControlOfThis", Triggers::youLoseControlOfThis,
                        CardType.CREATURE, (test, cards) -> {
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                test.changeControl(permanent, test.player2);
                                test.answerAll();
                            }
                        }),
                watch("an opponent gains control of a permanent you control",
                        "opponentGainsControlOfPermanentYouControl",
                        Triggers::opponentGainsControlOfPermanentYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.changeControl(test.addCreatureReady(test.player1, new GrizzlyBears()),
                                    test.player2);
                        }),
                watch("an Aura becomes attached to this", "auraBecomesAttachedToThis",
                        Triggers::auraBecomesAttachedToThis, CardType.CREATURE,
                        (test, cards) -> {
                            for (Permanent fixture : test.fixtureCreatures(cards)) {
                                test.auraBecomesAttachedTo(fixture);
                                test.answerAll();
                            }
                        }),
                watch("an Aura you control becomes attached to a creature you control",
                        "auraYouControlBecomesAttachedToCreatureYouControl",
                        Triggers::auraYouControlBecomesAttachedToCreatureYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.auraBecomesAttachedTo(test.addCreatureReady(test.player1, new GrizzlyBears()));
                        }),
                watch("an Aura you control becomes attached to a nonland permanent an opponent controls",
                        "auraYouControlBecomesAttachedToNonlandPermanentOpponentControls",
                        Triggers::auraYouControlBecomesAttachedToNonlandPermanentOpponentControls,
                        CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.auraBecomesAttachedTo(
                                    test.addCreatureReady(test.player2, card("Enchanted", CardType.CREATURE)));
                        }),
                watch("this becomes attached to a creature", "becomesAttachedToCreature",
                        Triggers::becomesAttachedToCreature, CardType.ARTIFACT,
                        TriggerRouteRecordTest::fixtureEquipmentBecomesAttached, CardSubtype.EQUIPMENT),
                watch("this becomes unattached from a permanent", "becomesUnattachedFromPermanent",
                        Triggers::becomesUnattachedFromPermanent, CardType.ARTIFACT,
                        TriggerRouteRecordTest::fixtureEquipmentBecomesUnattached, CardSubtype.EQUIPMENT),
                watch("you planeswalk to this", "planeswalkToThis", Triggers::planeswalkToThis, CardType.PLANE,
                        (test, cards) -> test.planarAbilityTriggers(cards, Triggers.planeswalkToThis())),
                watch("you planeswalk away from this", "planeswalkAwayFromThis", Triggers::planeswalkAwayFromThis,
                        CardType.PLANE,
                        (test, cards) -> test.planarAbilityTriggers(cards, Triggers.planeswalkAwayFromThis())),
                watch("you encounter this", "encounterThis", Triggers::encounterThis, CardType.PLANE,
                        (test, cards) -> test.planarAbilityTriggers(cards, Triggers.encounterThis())),
                watch("a state of the game", "state", () -> Triggers.state((game, source, controller) -> true),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::stateIsChecked),
                watch("a state of this", "sourceState", () -> Triggers.sourceState(new PermanentTruePredicate()),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::stateIsChecked),
                watch("the battlefield has at most", "battlefieldHasAtMost",
                        () -> Triggers.battlefieldHasAtMost(0,
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN))),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::stateIsChecked),
                watch("the battlefield has at least", "battlefieldHasAtLeast",
                        () -> Triggers.battlefieldHasAtLeast(1, new PermanentIsCreaturePredicate()),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::stateIsChecked),
                watch("you cast this spell", "youCastThisSpell", Triggers::youCastThisSpell, CardType.CREATURE,
                        (test, cards) -> {
                            test.mainPhaseOf(test.player1);
                            for (Card card : cards) {
                                test.gd.stack.add(new StackEntry(card, test.player1.getId()));
                                test.collect(collection ->
                                        collection.checkSpellCastTriggers(test.gd, card, test.player1.getId()));
                                test.answerAll();
                            }
                        }),
                watch("you copy a spell", "youCopySpell", () -> Triggers.youCopySpell().anySpell(),
                        CardType.ENCHANTMENT, (test, cards) -> test.spellIsCopied(cards, test.player1)),
                watch("an opponent copies a spell", "opponentCopiesSpell",
                        () -> Triggers.opponentCopiesSpell().anySpell(), CardType.ENCHANTMENT,
                        (test, cards) -> test.spellIsCopied(cards, test.player2)),
                collected("you counter a spell", "youCounterSpell", Triggers::youCounterSpell,
                        (test, collection) -> collection.checkControllerCountersSpellTriggers(test.gd,
                                test.player1.getId())),
                collected("your spell is countered", "yourSpellCountered", Triggers::yourSpellCountered,
                        (test, collection) -> collection.checkControllerSpellCounteredTriggers(test.gd,
                                test.player1.getId())),
                collected("your spell causes you to gain card advantage", "yourSpellCausesYouToGainCardAdvantage",
                        Triggers::yourSpellCausesYouToGainCardAdvantage,
                        (test, collection) -> collection.checkSpellCardAdvantageTriggers(test.gd, new StackEntry(
                                StackEntryType.SORCERY_SPELL, card("Advantage", CardType.SORCERY),
                                test.player1.getId(), "Advantage", List.of(new DrawCardEffect(2))))),
                watch("this spell is countered or fizzles", "thisSpellCounteredOrFizzles",
                        Triggers::thisSpellCounteredOrFizzles, CardType.INSTANT,
                        (test, cards) -> {
                            test.mainPhaseOf(test.player1);
                            for (Card card : cards) {
                                test.collect(collection -> collection.checkSelfSpellCounteredOrFizzledTriggers(
                                        test.gd, new StackEntry(StackEntryType.INSTANT_SPELL, card,
                                                test.player1.getId(), card.getName(), List.of())));
                                test.answerAll();
                            }
                        }),
                watch("you activate an ability", "youActivateAbility", Triggers::youActivateAbility,
                        CardType.ENCHANTMENT, (test, cards) -> test.abilityIsActivated(cards, test.player1)),
                watch("an opponent activates an ability", "opponentActivatesAbility",
                        Triggers::opponentActivatesAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.abilityIsActivated(cards, test.player2)),
                watch("you activate an ability that isn't a mana ability", "youActivateNonManaAbility",
                        Triggers::youActivateNonManaAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.abilityIsActivated(cards, test.player1)),
                watch("a player activates an ability that isn't a mana ability", "playerActivatesNonManaAbility",
                        Triggers::playerActivatesNonManaAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.abilityIsActivated(cards, test.player1)),
                watch("an opponent activates an ability that isn't a mana ability",
                        "opponentActivatesNonManaAbility", Triggers::opponentActivatesNonManaAbility,
                        CardType.ENCHANTMENT, (test, cards) -> test.abilityIsActivated(cards, test.player2)));
    }

    /** What a player or an object does that the first lists don't have: crimes, fights, crews, Rooms. */
    private static List<Watch> objectActionWatches() {
        return List.of(
                watch("a player loses the game", "playerLosesGame", Triggers::playerLosesGame, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            GameOutcomeService outcome = bean(GameOutcomeService.class);
                            test.harness.inMutationScope(() -> outcome.declareWinner(test.gd, test.player1.getId()));
                        }),
                watch("you commit a crime", "youCommitCrime", Triggers::youCommitCrime, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.castBoostAt(test.addCreatureReady(test.player2, new GrizzlyBears()));
                        }).with(ONCE_EACH_TURN),
                watch("an opponent commits a crime", "opponentCommitsCrime", Triggers::opponentCommitsCrime,
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            Permanent victim = test.addCreatureReady(test.player1, new GrizzlyBears());
                            Card boost = card("Boost", CardType.INSTANT);
                            boost.addSpellEffect(new BoostTargetCreatureEffect(1, 1));
                            test.mainPhaseOf(test.player2);
                            test.harness.setHand(test.player2, List.of(boost));
                            test.harness.addMana(test.player2, ManaColor.COLORLESS, 1);
                            test.harness.castInstant(test.player2, 0, victim.getId());
                        }),
                collected("a creature you control fights", "creatureYouControlFights",
                        Triggers::creatureYouControlFights,
                        (test, collection) -> collection.checkAllyCreatureFightsTriggers(test.gd,
                                test.addCreatureReady(test.player1, new GrizzlyBears()), test.player1.getId())),
                watch("a creature you control explores", "creatureYouControlExplores",
                        Triggers::creatureYouControlExplores, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.harness.setLibrary(test.player1, List.of(new Forest()));
                            test.helperAbilityResolves(cards, new ExploreEffect());
                        }),
                watch("a creature you control connives", "creatureYouControlConnives",
                        Triggers::creatureYouControlConnives, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.harness.setHand(test.player1, List.of());
                            test.harness.setLibrary(test.player1, List.of(new Forest()));
                            test.helperAbilityResolves(cards, new DrawDiscardAndConniveEffect());
                            test.harness.handleCardChosen(test.player1, 0);
                        }),
                watch("this exploits a creature", "exploits", Triggers::exploits, CardType.CREATURE,
                        (test, cards) -> {
                            test.mainPhaseOf(test.player1);
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                test.collect(collection -> collection.checkExploitTriggers(test.gd,
                                        permanent.getCard(), test.player1.getId(), permanent.getId()));
                                test.answerAll();
                            }
                        }),
                watch("a creature you control exploits a creature", "creatureYouControlExploits",
                        Triggers::creatureYouControlExploits, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::helperExploits),
                watch("a creature you control exploits a nontoken creature",
                        "creatureYouControlExploitsNontokenCreature",
                        Triggers::creatureYouControlExploitsNontokenCreature, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::helperExploits),
                watch("this champions a creature", "championsCreature", Triggers::championsCreature,
                        CardType.CREATURE, (test, cards) -> {
                            for (Card card : cards) {
                                card.addTrigger(Triggers.enters(ObjectRelation.THIS),
                                        new ChampionCreatureEffect(CardSubtype.GOBLIN));
                                Permanent fodder = test.addCreatureReady(test.player1,
                                        card("Champion Fodder", CardType.CREATURE, CardSubtype.GOBLIN));
                                test.mainPhaseOf(test.player1);
                                test.harness.enterBattlefieldAndReturn(test.player1, card);
                                test.harness.passBothPriorities();
                                test.harness.handlePermanentChosen(test.player1, fodder.getId());
                                test.answerAll();
                            }
                        }),
                watch("this becomes plotted", "becomesPlotted", Triggers::becomesPlotted, CardType.CREATURE,
                        (test, cards) -> {
                            test.mainPhaseOf(test.player1);
                            for (Card card : cards) {
                                test.collect(collection -> {
                                    test.gd.addToExile(test.player1.getId(), card);
                                    test.gd.plottedCardIds.add(card.getId());
                                    collection.checkPlotTriggers(test.gd, test.player1.getId(), card);
                                });
                                test.answerAll();
                            }
                        }),
                watch("this mutates", "mutates", Triggers::mutates, CardType.CREATURE,
                        (test, cards) -> {
                            test.mainPhaseOf(test.player1);
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                test.collect(collection -> collection.checkMutateTriggers(test.gd, permanent,
                                        List.of(permanent.getCard()), test.player1.getId()));
                                test.answerAll();
                            }
                        }),
                collected("a creature you control mutates", "creatureYouControlMutates",
                        Triggers::creatureYouControlMutates,
                        (test, collection) -> {
                            Permanent mutated = test.addCreatureReady(test.player1, new GrizzlyBears());
                            collection.checkMutateTriggers(test.gd, mutated, List.of(mutated.getCard()),
                                    test.player1.getId());
                        }),
                watch("this crews a Vehicle", "crewsVehicle", Triggers::crewsVehicle, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesCrewAVehicle),
                watch("a creature you control crews a Vehicle", "creatureYouControlCrewsVehicle",
                        Triggers::creatureYouControlCrewsVehicle, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.crewAVehicleWith(test.addCreatureReady(test.player1, new GrizzlyBears()));
                        }),
                watch("this saddles a Mount or crews a Vehicle during your main phase",
                        "saddlesOrCrewsDuringMainPhase", Triggers::saddlesOrCrewsDuringMainPhase, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesCrewAVehicle),
                watch("this becomes crewed", "becomesCrewed", Triggers::becomesCrewed, CardType.ARTIFACT,
                        (test, cards) -> test.fixturesAreCollected(cards, (collection, vehicle) ->
                                collection.checkBecomesCrewedTriggers(test.gd, vehicle, test.player1.getId())),
                        CardSubtype.VEHICLE),
                watch("this becomes crewed for the first time each turn", "becomesCrewedForFirstTimeEachTurn",
                        Triggers::becomesCrewedForFirstTimeEachTurn, CardType.ARTIFACT,
                        (test, cards) -> test.fixturesAreCollected(cards, (collection, vehicle) ->
                                collection.checkBecomesCrewedTriggers(test.gd, vehicle, test.player1.getId())),
                        CardSubtype.VEHICLE),
                watch("this becomes saddled", "becomesSaddled", Triggers::becomesSaddled, CardType.CREATURE,
                        (test, cards) -> test.fixturesAreCollected(cards, (collection, mount) ->
                                collection.checkBecomesSaddledTriggers(test.gd, mount, test.player1.getId()))),
                watch("this becomes saddled for the first time each turn", "becomesSaddledForFirstTimeEachTurn",
                        Triggers::becomesSaddledForFirstTimeEachTurn, CardType.CREATURE,
                        (test, cards) -> test.fixturesAreCollected(cards, (collection, mount) ->
                                collection.checkBecomesSaddledTriggers(test.gd, mount, test.player1.getId()))),
                watch("this becomes stationed", "becomesStationed", Triggers::becomesStationed, CardType.ARTIFACT,
                        TriggerRouteRecordTest::fixturesAreStationed),
                watch("a permanent entering causes an ability to trigger", "permanentEnteringCausesAbilityToTrigger",
                        Triggers::permanentEnteringCausesAbilityToTrigger, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card helper = card("Helper", CardType.CREATURE);
                            helper.addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1));
                            test.mainPhaseOf(test.player1);
                            test.harness.enterBattlefieldAndReturn(test.player1, helper);
                        }),
                watch("you unlock this door", "doorUnlocked", () -> Triggers.doorUnlocked(0), CardType.ENCHANTMENT,
                        (test, cards) -> test.fixturesAreCollected(cards, (collection, room) -> {
                            room.unlockRoomDoor(0);
                            collection.checkSelfRoomDoorUnlockedTriggers(test.gd, test.player1.getId(), room, 0);
                        }), CardSubtype.ROOM),
                collected("you fully unlock a Room", "youFullyUnlockRoom", Triggers::youFullyUnlockRoom,
                        (test, collection) -> {
                            Permanent room = test.harness.addToBattlefieldAndReturn(test.player1,
                                    card("Fixture Room", CardType.ENCHANTMENT, CardSubtype.ROOM));
                            room.unlockRoomDoor(0);
                            room.unlockRoomDoor(1);
                            collection.checkAllyRoomFullyUnlockedTriggers(test.gd, test.player1.getId(), room);
                        }));
    }

    /** The counter triggers the first lists don't have. */
    private static List<Watch> counterWatches() {
        return List.of(
                watch("a creature deals damage to its controller", "creatureDealsDamageToItsController",
                        Triggers::creatureDealsDamageToItsController, CardType.PLANE,
                        TriggerRouteRecordTest::opponentCreatureDamagesItsController),
                watch("a time counter is removed from this in exile", "timeCounterRemovedFromThisInExile",
                        Triggers::timeCounterRemovedFromThisInExile, CardType.CREATURE,
                        TriggerRouteRecordTest::timeCounterIsRemovedFromExiledFixtures),
                watch("you remove the last counter from this", "youRemoveLastCounterFromThis",
                        () -> Triggers.youRemoveLastCounterFromThis(CounterType.CHARGE), CardType.CREATURE,
                        (test, cards) -> {
                            PermanentCounterSupport counters = bean(PermanentCounterSupport.class);
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                test.harness.inMutationScope(() -> counters.notifyLastCounterRemoved(test.gd,
                                        permanent, CounterType.CHARGE, 1, test.player1.getId()));
                                test.answerAll();
                            }
                        }),
                watch("you remove a time counter from this in exile", "youRemoveTimeCounterFromThisInExile",
                        Triggers::youRemoveTimeCounterFromThisInExile, CardType.CREATURE,
                        TriggerRouteRecordTest::timeCounterIsRemovedFromExiledFixtures),
                watch("this becomes renowned", "becomesRenowned", Triggers::becomesRenowned, CardType.CREATURE,
                        (test, cards) -> test.fixturesAreCollected(cards, (collection, permanent) ->
                                collection.checkBecomesRenownedTriggers(test.gd, permanent, test.player1.getId()))),
                watch("a creature you control becomes renowned", "creatureYouControlBecomesRenowned",
                        Triggers::creatureYouControlBecomesRenowned, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new RenownEffect(1))),
                watch("this becomes monstrous", "becomesMonstrous", Triggers::becomesMonstrous, CardType.CREATURE,
                        (test, cards) -> test.fixturesAreCollected(cards, (collection, permanent) ->
                                collection.checkBecomesMonstrousTriggers(test.gd, permanent, test.player1.getId()))),
                watch("you get energy", "youGetEnergy", Triggers::youGetEnergy, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new EnergyCountersEffect(1))),
                watch("you put counters on a creature you don't control", "youPutCountersOnCreatureYouDontControl",
                        Triggers::youPutCountersOnCreatureYouDontControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.countersArePutOnOpponentCreature(cards, test.player1)),
                watch("this trains", "trains", Triggers::trains, CardType.CREATURE,
                        (test, cards) -> test.fixturesResolve(cards, new TrainEffect(), TrainEffectHandler.class,
                                TrainEffectHandler::resolve)),
                watch("+1/+1 counters put on a Human you control", "plusOneCountersPutOnHumanYouControl",
                        Triggers::plusOneCountersPutOnHumanYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.countersArePutOnHelper(cards, CardSubtype.HUMAN)),
                watch("this Class becomes level 2", "reachesLevelTwo", Triggers::reachesLevelTwo,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.fixturesResolve(cards, new ClassLevelUpEffect(2),
                                ClassLevelUpEffectHandler.class, ClassLevelUpEffectHandler::resolve)),
                watch("this Class becomes level 3", "reachesLevelThree", Triggers::reachesLevelThree,
                        CardType.ENCHANTMENT,
                        (test, cards) -> test.fixturesResolve(cards, new ClassLevelUpEffect(3),
                                ClassLevelUpEffectHandler.class, (handler, game, entry, effect) -> {
                                    game.playerBattlefields.get(test.player1.getId()).stream()
                                            .filter(permanent ->
                                                    permanent.getId().equals(entry.getSourcePermanentId()))
                                            .forEach(permanent -> permanent.setClassLevel(2));
                                    handler.resolve(game, entry, effect);
                                })),
                watch("+1/+1 counters put on another non-Hydra creature you control",
                        "plusOneCountersPutOnAnotherNonHydraCreatureYouControl",
                        Triggers::plusOneCountersPutOnAnotherNonHydraCreatureYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards, new PutCountersOnSourceEffect(1, 1, 1))),
                watch("+1/+1 counters put on another permanent you control for the first time each turn", "plusOneCountersPutOnAnotherPermanentYouControlFirstTimeEachTurn", Triggers::plusOneCountersPutOnAnotherPermanentYouControlFirstTimeEachTurn, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))),
                watch("you put +1/+1 counters on a permanent you control", "youPutPlusOneCountersOnPermanentYouControl", Triggers::youPutPlusOneCountersOnPermanentYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))),
                watch("you put +1/+1 counters on a creature you control", "youPutPlusOneCountersOnCreatureYouControl", Triggers::youPutPlusOneCountersOnCreatureYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))),
                watch("+1/+1 counters put on a creature you control", "plusOneCountersPutOnCreatureYouControl", Triggers::plusOneCountersPutOnCreatureYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))),
                watch("you put +1/+1 counters on a creature", "youPutPlusOneCountersOnCreature", Triggers::youPutPlusOneCountersOnCreature, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))),
                watch("you put +1/+1 counters on another Hero you control",
                        "youPutPlusOneCountersOnAnotherHeroYouControl",
                        Triggers::youPutPlusOneCountersOnAnotherHeroYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.countersArePutOnHelper(cards, CardSubtype.HERO)),
                watch("you put +1/+1 counters on another creature", "youPutPlusOneCountersOnAnotherCreature", Triggers::youPutPlusOneCountersOnAnotherCreature, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))),
                watch("you put time counters on a permanent you control", "youPutTimeCountersOnPermanentYouControl",
                        Triggers::youPutTimeCountersOnPermanentYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.helperAbilityResolves(cards,
                                new PutCountersOnSelfEffect(CounterType.TIME, 1))),
                watch("an opponent puts counters on a creature they control",
                        "opponentPutsCountersOnCreatureTheyControl",
                        Triggers::opponentPutsCountersOnCreatureTheyControl, CardType.ENCHANTMENT,
                        (test, cards) -> test.countersArePutOnOpponentCreature(cards, test.player2)),
                collected("you put a lore counter on a Saga you control", "youPutLoreCounterOnSagaYouControl",
                        Triggers::youPutLoreCounterOnSagaYouControl,
                        (test, collection) -> collection.checkYouPutLoreCounterOnSagaTriggers(test.gd,
                                test.harness.addToBattlefieldAndReturn(test.player1,
                                        card("Saga", CardType.ENCHANTMENT, CardSubtype.SAGA)),
                                test.player1.getId())),
                watch("this evolves", "evolves", Triggers::evolves, CardType.CREATURE,
                        (test, cards) -> test.fixturesResolve(cards, new EvolveEffect(), EvolveEffectHandler.class,
                                EvolveEffectHandler::resolve)),
                watch("loyalty counters are removed from this", "loyaltyCountersRemovedFromThis", Triggers::loyaltyCountersRemovedFromThis, CardType.CREATURE,
                        (test, cards) -> test.countersAreRemoved(test.fixtureCreatures(cards), CounterType.LOYALTY)),
                watch("time counters are removed from this", "timeCountersRemovedFromThis", Triggers::timeCountersRemovedFromThis, CardType.CREATURE,
                        (test, cards) -> test.countersAreRemoved(test.fixtureCreatures(cards), CounterType.TIME)),
                watch("a +1/+1 counter is removed from this", "plusOneCounterRemovedFromThis", Triggers::plusOneCounterRemovedFromThis, CardType.CREATURE,
                        (test, cards) -> test.countersAreRemoved(test.fixtureCreatures(cards), CounterType.PLUS_ONE_PLUS_ONE)),
                watch("counters are removed from a permanent you control", "countersRemovedFromPermanentYouControl",
                        Triggers::countersRemovedFromPermanentYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.countersAreRemoved(List.of(test.addCreatureReady(test.player1, new GrizzlyBears())),
                                    CounterType.PLUS_ONE_PLUS_ONE);
                        }));
    }

    /** The -1/-1 and loyalty counter, "becomes the target" and draw triggers the first lists don't have. */
    private static List<Watch> targetAndDrawWatches() {
        return List.of(
                watch("loyalty counters put on planeswalkers you control",
                        "loyaltyCountersPutOnPlaneswalkersYouControl",
                        Triggers::loyaltyCountersPutOnPlaneswalkersYouControl, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.harness.addToBattlefieldAndReturn(test.player1,
                                    card("Walker", CardType.PLANESWALKER)).setCounterCount(CounterType.LOYALTY, 3);
                            test.helperAbilityResolves(cards, new PutCounterOnEachControlledPermanentEffect(
                                    CounterType.LOYALTY, 1, Triggers.PLANESWALKER));
                        }),
                watch("you put -1/-1 counters on this", "youPutMinusOneCountersOnThis",
                        Triggers::youPutMinusOneCountersOnThis, CardType.CREATURE,
                        (test, cards) -> {
                            test.fixtureCreatures(cards);
                            test.helperEntersAndItsAbilityResolves(new PutCounterOnEachControlledPermanentEffect(
                                    CounterType.MINUS_ONE_MINUS_ONE, 1, Triggers.CREATURE));
                        }),
                watch("a -1/-1 counter is put on a creature", "minusOneCounterPutOnCreature", Triggers::minusOneCounterPutOnCreature, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::minusOneCounterIsPutOnACreature),
                watch("-1/-1 counters are put on a creature", "minusOneCountersPutOnCreature", Triggers::minusOneCountersPutOnCreature, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::minusOneCounterIsPutOnACreature),
                watch("you put a -1/-1 counter on a creature", "youPutMinusOneCounterOnCreature", Triggers::youPutMinusOneCounterOnCreature, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::minusOneCounterIsPutOnACreature),
                watch("you put -1/-1 counters on a creature", "youPutMinusOneCountersOnCreature", Triggers::youPutMinusOneCountersOnCreature, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::minusOneCounterIsPutOnACreature),
                watch("a player chooses targets", "playerChoosesTargets", Triggers::playerChoosesTargets,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::anotherCreatureBecomesSpellTarget),
                watch("a player or permanent becomes the target of your ability",
                        "playerOrPermanentBecomesTargetOfYourAbility",
                        Triggers::playerOrPermanentBecomesTargetOfYourAbility, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.youActivateAPingAt(test.player2.getId());
                        }),
                watch("this becomes the target of an Aura spell", "becomesTargetOfAuraSpell",
                        Triggers::becomesTargetOfAuraSpell, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBecomeAuraSpellTargets),
                watch("this becomes the target of an opponent's spell or ability",
                        "becomesTargetOfOpponentSpellOrAbility", Triggers::becomesTargetOfOpponentSpellOrAbility,
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesBecomeOpponentSpellTargets),
                watch("this becomes the target of an opponent's spell", "becomesTargetOfOpponentSpell",
                        Triggers::becomesTargetOfOpponentSpell, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBecomeOpponentSpellTargets),
                watch("you become the target of a spell", "youBecomeTargetOfSpell", Triggers::youBecomeTargetOfSpell, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.player1.getId())),
                watch("you become the target of an opponent's spell or ability", "youBecomeTargetOfOpponentSpellOrAbility", Triggers::youBecomeTargetOfOpponentSpellOrAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.player1.getId())),
                watch("this becomes the target of your spell or ability", "becomesTargetOfYourSpellOrAbility",
                        Triggers::becomesTargetOfYourSpellOrAbility, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesBecomeSpellTargets),
                watch("a creature you control becomes the target of an opponent's spell or ability", "creatureYouControlBecomesTargetOfOpponentSpellOrAbility", Triggers::creatureYouControlBecomesTargetOfOpponentSpellOrAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.addCreatureReady(test.player1, new GrizzlyBears()).getId())),
                watch("creatures you control become the target of an activated ability",
                        "creaturesYouControlBecomeTargetOfActivatedAbility",
                        Triggers::creaturesYouControlBecomeTargetOfActivatedAbility, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.youActivateAPingAt(test.addCreatureReady(test.player1, new GrizzlyBears()).getId());
                        }),
                watch("a creature you control becomes the target of an opponent's spell", "creatureYouControlBecomesTargetOfOpponentSpell", Triggers::creatureYouControlBecomesTargetOfOpponentSpell, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.addCreatureReady(test.player1, new GrizzlyBears()).getId())),
                watch("a creature or creature spell you control becomes the target of an opponent's spell or ability", "creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility", Triggers::creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.addCreatureReady(test.player1, new GrizzlyBears()).getId())),
                watch("you or a permanent you control becomes the target of an opponent's spell or ability", "youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility", Triggers::youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.player1.getId())),
                watch("a permanent you control becomes the target of an opponent's spell or ability", "permanentYouControlBecomesTargetOfOpponentSpellOrAbility", Triggers::permanentYouControlBecomesTargetOfOpponentSpellOrAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.addCreatureReady(test.player1, new GrizzlyBears()).getId())),
                watch("another permanent you control becomes the target of an opponent's spell or ability", "anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility", Triggers::anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentTargets(cards, test.addCreatureReady(test.player1, new GrizzlyBears()).getId())),
                watch("a creature an opponent controls becomes the target of your spell or ability",
                        "creatureOpponentControlsBecomesTargetOfYourSpellOrAbility",
                        Triggers::creatureOpponentControlsBecomesTargetOfYourSpellOrAbility, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.castBoostAt(test.firstTarget);
                        }),
                watch("a creature you control becomes the target of a backup ability",
                        "creatureYouControlBecomesTargetOfBackupAbility",
                        Triggers::creatureYouControlBecomesTargetOfBackupAbility, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::backupAbilityTargetsYourCreature),
                watch("a creature you control becomes the target of an instant or sorcery spell",
                        "creatureYouControlBecomesTargetOfInstantOrSorcerySpell",
                        Triggers::creatureYouControlBecomesTargetOfInstantOrSorcerySpell, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureBecomesSpellTarget),
                watch("you draw your first card in your draw step", "youDrawFirstCardInDrawStep",
                        Triggers::youDrawFirstCardInDrawStep, CardType.ENCHANTMENT,
                        (test, cards) -> test.stepBegins(cards, TurnStep.UPKEEP, TurnStep.DRAW)),
                watch("an opponent draws their second card each turn", "opponentDrawsSecondCardEachTurn",
                        Triggers::opponentDrawsSecondCardEachTurn, CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentDraws(cards, 2)),
                watch("an opponent draws their nth card each turn", "opponentDrawsNthCardEachTurn",
                        () -> Triggers.opponentDrawsNthCardEachTurn(1), CardType.ENCHANTMENT,
                        (test, cards) -> test.opponentDraws(cards, 1)),
                watch("a player draws a card", "playerDrawsCard", Triggers::playerDrawsCard, CardType.EMBLEM,
                        (test, cards) -> {
                            test.mainPhaseOf(test.player1);
                            for (Card card : cards) {
                                test.gd.emblems.add(new Emblem(test.player1.getId(), List.of(), card, List.of(),
                                        test.gd.nextTimestamp(), card.getTriggeredAbilities()));
                            }
                            test.helperEntersAndItsAbilityResolves(new DrawCardEffect(1));
                        }),
                watch("an opponent draws a card", "opponentDrawsCard", Triggers::opponentDrawsCard,
                        CardType.ENCHANTMENT, (test, cards) -> test.opponentDraws(cards, 1)));
    }

    /** The zone-change, phasing, transform, step, land-play, life and Saga triggers the first lists don't have. */
    private static List<Watch> zoneAndStepWatches() {
        return List.of(
                watch("this is put into a graveyard while resolving", "putIntoGraveyardWhileResolving",
                        () -> Triggers.putIntoGraveyardWhileResolving(ObjectRelation.THIS), CardType.INSTANT,
                        (test, cards) -> {
                            for (Card card : cards) {
                                card.addSpellEffect(new GainLifeEffect(1));
                                test.mainPhaseOf(test.player1);
                                test.harness.setHand(test.player1, List.of(card));
                                test.harness.addMana(test.player1, ManaColor.COLORLESS, 1);
                                test.harness.castAndResolveInstant(test.player1, 0);
                                test.answerAll();
                            }
                        }),
                watch("this is milled", "milled", () -> Triggers.milled(ObjectRelation.THIS), CardType.CREATURE,
                        (test, cards) -> {
                            test.harness.setLibrary(test.player1, cards);
                            test.helperEntersAndItsAbilityResolves(new MillEffect(2, MillRecipient.CONTROLLER));
                        }),
                watch("a noncreature artifact is sacrificed or destroyed", "sacrificedOrDestroyed",
                        () -> Triggers.sacrificedOrDestroyed(Triggers.NONCREATURE_ARTIFACT, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.destroy(test.harness.addToBattlefieldAndReturn(test.player2,
                                    card("Relic", CardType.ARTIFACT)));
                        }),
                watch("a noncreature permanent you control is destroyed by an opponent", "destroyed",
                        () -> Triggers.destroyed(Triggers.NONCREATURE, ObjectRelation.YOU_CONTROL)
                                .causedBy(PlayerRelation.OPPONENT),
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.harness.addToBattlefield(test.player1,
                                    card("Victim", CardType.ARTIFACT, CardSubtype.EQUIPMENT));
                            test.opponentHelperAbilityResolves(new DestroyAllPermanentsEffect(
                                    new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.EQUIPMENT))));
                        }),
                watch("another creature you control leaves the battlefield without dying",
                        "leavesBattlefieldWithoutDying",
                        () -> Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE,
                                ObjectRelation.ANOTHER_YOU_CONTROL),
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            Permanent leaving = test.addCreatureReady(test.player1, new GrizzlyBears());
                            test.removal(removal -> removal.removePermanentToHand(test.gd, leaving));
                        }),
                watch("another nontoken artifact you control is put into a graveyard or exile",
                        "putIntoGraveyardOrExileFromBattlefield",
                        () -> Triggers.putIntoGraveyardOrExileFromBattlefield(Triggers.NONTOKEN_ARTIFACT,
                                ObjectRelation.ANOTHER_YOU_CONTROL),
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.destroy(test.harness.addToBattlefieldAndReturn(test.player1,
                                    card("Relic", CardType.ARTIFACT)));
                        }),
                watch("a nontoken creature card is put into a graveyard from anywhere but the battlefield",
                        "putIntoGraveyardFromAnywhereButBattlefield",
                        () -> Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_CREATURE,
                                ObjectRelation.ANY),
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.harness.setLibrary(test.player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
                            test.helperAbilityResolves(cards, new MillEffect(1, MillRecipient.CONTROLLER));
                        }),
                watch("another nontoken creature you own is put into a graveyard from anywhere but your library",
                        "putIntoGraveyardFromAnywhereBut",
                        () -> Triggers.putIntoGraveyardFromAnywhereBut(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,
                                ObjectRelation.ANOTHER_YOU_OWN),
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::yourCreatureIsDestroyed),
                watch("a player mills a nonland card", "mills",
                        () -> Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY), CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.harness.setLibrary(test.player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
                            test.helperAbilityResolves(cards, new MillEffect(1, MillRecipient.EACH_OPPONENT));
                        }),
                watch("this is exiled while you activate a craft ability", "exiledWhileActivatingCraftAbility",
                        () -> Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS), CardType.CREATURE,
                        (test, cards) -> {
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                test.removal(removal -> removal.removePermanentToExileAsCraftMaterial(test.gd,
                                        permanent, permanent.getId()));
                                test.answerAll();
                            }
                        }),
                watch("this is exiled from the battlefield", "exiledFrom",
                        () -> Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS),
                        CardType.CREATURE, (test, cards) -> {
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                test.removal(removal -> removal.removePermanentToExile(test.gd, permanent));
                                test.answerAll();
                            }
                        }),
                watch("cards leave your graveyard", "leavesGraveyard",
                        () -> Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN), CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.mainPhaseOf(test.player1);
                            GraveyardService graveyards = bean(GraveyardService.class);
                            test.harness.inMutationScope(() ->
                                    graveyards.exileCardsFromGraveyard(test.gd, test.player1.getId(), 1));
                        }).with(TriggerOptions.ONE_OR_MORE),
                watch("a permanent is put into a hand from the battlefield", "putIntoHandFrom",
                        () -> Triggers.putIntoHandFrom(Zone.BATTLEFIELD, null, ObjectRelation.ANY),
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            Permanent returned = test.addCreatureReady(test.player1, new GrizzlyBears());
                            test.removal(removal -> removal.removePermanentToHand(test.gd, returned));
                        }),
                watch("other nontoken permanents are put into a library", "putIntoLibrary",
                        () -> Triggers.putIntoLibrary(Triggers.NONTOKEN, ObjectRelation.ANOTHER),
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            Permanent tucked = test.addCreatureReady(test.player1, new GrizzlyBears());
                            test.removal(removal -> removal.removePermanentToLibraryTop(test.gd, tucked));
                        }).with(TriggerOptions.ONE_OR_MORE),
                watch("a commander is put into the command zone", "putIntoCommandZone",
                        () -> Triggers.putIntoCommandZone(null, ObjectRelation.YOU_OWN), CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card commander = card("Commander", CardType.CREATURE);
                            Permanent permanent = test.addCreatureReady(test.player1, commander);
                            test.gd.playerCommanders.put(test.player1.getId(), new ArrayList<>(List.of(commander)));
                            test.removal(removal -> removal.removePermanentToCommandZone(test.gd, permanent));
                        }),
                watch("this enters from a graveyard", "entersFrom",
                        () -> Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS), CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesEnterFromGraveyard),
                watch("this phases out", "phasesOut", () -> Triggers.phasesOut(null, ObjectRelation.THIS),
                        CardType.CREATURE, (test, cards) -> {
                            PhasingService phasing = bean(PhasingService.class);
                            for (Permanent permanent : test.fixtureCreatures(cards)) {
                                test.harness.inMutationScope(() -> phasing.phaseOut(test.gd, List.of(permanent)));
                                test.answerAll();
                            }
                        }),
                watch("this phases in", "phasesIn", () -> Triggers.phasesIn(null, ObjectRelation.THIS),
                        CardType.CREATURE, (test, cards) -> {
                            PhasingService phasing = bean(PhasingService.class);
                            List<Permanent> permanents = test.fixtureCreatures(cards);
                            test.harness.inMutationScope(() -> phasing.phaseOut(test.gd, permanents));
                            test.harness.inMutationScope(() -> phasing.applyPhasing(test.gd, test.player1.getId()));
                            test.answerAll();
                            StepTriggerService steps = bean(StepTriggerService.class);
                            test.harness.inMutationScope(() -> steps.processNextPhasesInTriggerTarget(test.gd));
                        }),
                watch("a permanent you control transforms", "transforms",
                        () -> Triggers.transforms(null, ObjectRelation.YOU_CONTROL), CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card front = card("Shifter", CardType.CREATURE);
                            front.setBackFaceCard(card("Shifter", CardType.CREATURE));
                            Permanent permanent = test.addCreatureReady(test.player1, front);
                            TransformChosenPermanentEffectHandler transformer =
                                    bean(TransformChosenPermanentEffectHandler.class);
                            test.harness.inMutationScope(() -> transformer.transform(test.gd, permanent.getId()));
                        }),
                watch("transforms into its front face", "transformsToFrontFace",
                        () -> Triggers.transformsToFrontFace(ObjectRelation.THIS), CardType.CREATURE,
                        (test, cards) -> {
                            TransformChosenPermanentEffectHandler transformer =
                                    bean(TransformChosenPermanentEffectHandler.class);
                            for (Card front : cards) {
                                front.setBackFaceCard(card(front.getName(), CardType.CREATURE));
                                Permanent permanent = test.addCreatureReady(test.player1, front);
                                test.harness.inMutationScope(() -> transformer.transform(test.gd, permanent.getId()));
                                test.harness.inMutationScope(() -> transformer.transform(test.gd, permanent.getId()));
                                test.answerAll();
                            }
                        }),
                watch("end of combat", "endOfCombat", Triggers::endOfCombat, CardType.ENCHANTMENT,
                        (test, cards) -> test.stepIsHandled(cards, TurnStep.END_OF_COMBAT,
                                StepTriggerService::handleEndOfCombatTriggers)),
                watch("your postcombat main phase", "postcombatMainPhase",
                        () -> Triggers.postcombatMainPhase(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        (test, cards) -> test.stepIsHandled(cards, TurnStep.POSTCOMBAT_MAIN,
                                StepTriggerService::handlePostcombatMainTriggers)),
                watch("your second main phase", "secondMainPhase",
                        () -> Triggers.secondMainPhase(ActivePlayer.YOU), CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.gd.mainPhasesBegunThisTurn = 1;
                            test.stepIsHandled(cards, TurnStep.POSTCOMBAT_MAIN,
                                    StepTriggerService::handlePostcombatMainTriggers);
                        }),
                watch("you play another land", "youPlayAnotherLand", Triggers::youPlayAnotherLand,
                        CardType.ENCHANTMENT, TriggerRouteRecordTest::youPlayALand),
                watch("an opponent plays a land", "opponentPlaysLand", Triggers::opponentPlaysLand,
                        CardType.ENCHANTMENT, (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.mainPhaseOf(test.player2);
                            test.harness.setHand(test.player2, List.of(card("Fixture Land", CardType.LAND)));
                            test.harness.playLand(test.player2, 0);
                        }),
                watch("an opponent loses life for the first time during their turn",
                        "opponentLosesLifeForFirstTimeDuringTheirTurn",
                        Triggers::opponentLosesLifeForFirstTimeDuringTheirTurn, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card helper = card("Helper", CardType.CREATURE);
                            helper.addTrigger(Triggers.enters(ObjectRelation.THIS),
                                    new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
                            test.mainPhaseOf(test.player2);
                            test.harness.enterBattlefieldAndReturn(test.player1, helper);
                            test.harness.passBothPriorities();
                        }),
                collected("you pay life", "youPayLife", Triggers::youPayLife,
                        (test, collection) -> collection.checkLifePaymentTriggers(test.gd, test.player1.getId(), 2)),
                watch("a Saga's chapter I", "chapter", () -> Triggers.chapter(1), CardType.ENCHANTMENT,
                        (test, cards) -> {
                            cards.forEach(card -> card.addTrigger(Triggers.chapter(2), new GainLifeEffect(1)));
                            test.stepBegins(cards, TurnStep.DRAW, TurnStep.PRECOMBAT_MAIN);
                        }, CardSubtype.SAGA));
    }

    /** The activation, attack, block and damage triggers the first lists don't have. */
    private static List<Watch> combatAndDamageWatches() {
        return List.of(
                watch("you activate an exhaust ability", "youActivateExhaustAbility",
                        Triggers::youActivateExhaustAbility, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card exhauster = card("Exhauster", CardType.CREATURE);
                            exhauster.addActivatedAbility(new ActivatedAbility(false, "{1}",
                                    List.of(new GainLifeEffect(1)), "Exhaust").withExhaust());
                            Permanent source = test.addCreatureReady(test.player1, exhauster);
                            test.mainPhaseOf(test.player1);
                            test.harness.addMana(test.player1, ManaColor.COLORLESS, 1);
                            test.harness.activateAbility(test.player1,
                                    test.gd.playerBattlefields.get(test.player1.getId()).indexOf(source), 0, null,
                                    null);
                        }),
                watch("you activate an eternalize or embalm ability", "youActivateEternalizeOrEmbalm",
                        Triggers::youActivateEternalizeOrEmbalm, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card eternal = card("Eternal", CardType.CREATURE);
                            eternal.addEternalize("{1}", "Cat");
                            List<Card> graveyard = test.gd.playerGraveyards.get(test.player1.getId());
                            graveyard.add(eternal);
                            test.mainPhaseOf(test.player1);
                            test.harness.addMana(test.player1, ManaColor.COLORLESS, 1);
                            test.harness.activateGraveyardAbility(test.player1, graveyard.indexOf(eternal));
                        }),
                watch("this mana ability resolves", "thisManaAbilityResolves", Triggers::thisManaAbilityResolves,
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesAreTappedForMana),
                watch("a player attacks one of your opponents", "playerAttacksOneOfYourOpponents",
                        Triggers::playerAttacksOneOfYourOpponents, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::anotherCreatureAttacks),
                watch("an opponent attacks another opponent", "opponentAttacksAnotherOpponent",
                        Triggers::opponentAttacksAnotherOpponent, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::opponentAttacksAThirdPlayer),
                watch("an opponent attacks your planeswalker", "opponentAttacksYourPlaneswalker",
                        Triggers::opponentAttacksYourPlaneswalker, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Permanent walker = test.harness.addToBattlefieldAndReturn(test.player1,
                                    card("Walker", CardType.PLANESWALKER));
                            walker.setCounterCount(CounterType.LOYALTY, 5);
                            test.opponentAttacks(walker.getId());
                        }),
                watch("an ability of an attacking creature you control triggers",
                        "attackingCreatureYouControlAbilityTriggers",
                        Triggers::attackingCreatureYouControlAbilityTriggers, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card attackerCard = card("Attack Trigger", CardType.CREATURE);
                            attackerCard.addTrigger(Triggers.attacks(), new GainLifeEffect(1));
                            Permanent attacker = test.addCreatureReady(test.player1, attackerCard);
                            int index = test.gd.playerBattlefields.get(test.player1.getId()).indexOf(attacker);
                            test.harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                                    () -> test.declareAttackers(test.player1, List.of(index)));
                        }),
                watch("this enlists a creature", "enlists", Triggers::enlists, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesEnlist),
                watch("creatures attack you and aren't blocked", "creaturesAttackYouUnblocked",
                        Triggers::creaturesAttackYouUnblocked, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Permanent attacker = test.addCreatureReady(test.player2, new GrizzlyBears());
                            attacker.setAttacking(true);
                            attacker.setAttackTarget(test.player1.getId());
                            test.harness.forceActivePlayer(test.player2);
                            test.prepareDeclareBlockers(test.player2);
                            test.gs.declareBlockers(test.gd, test.player1, List.of());
                        }),
                watch("this blocks two or more creatures", "blocksTwoOrMore", Triggers::blocksTwoOrMore,
                        CardType.CREATURE, TriggerRouteRecordTest::fixturesBlockTwo),
                watch("a creature with the same name deals combat damage to a player",
                        "creatureWithSameNameDealsCombatDamageToPlayer",
                        Triggers::creatureWithSameNameDealsCombatDamageToPlayer, CardType.CREATURE,
                        TriggerRouteRecordTest::fixturesDealCombatDamageToPlayer),
                watch("goaded creatures deal combat damage to an opponent",
                        "goadedCreaturesDealCombatDamageToOpponent",
                        Triggers::goadedCreaturesDealCombatDamageToOpponent, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            Card goaded = card("Goaded", CardType.CREATURE);
                            goaded.addStaticEffect(new GoadSourceCreatureEffect());
                            test.addCreatureReady(test.player1, goaded).setAttacking(true);
                            test.harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> test.resolveCombat());
                        }),
                watch("creatures you control deal combat damage to an opponent",
                        "creaturesYouControlDealCombatDamageToOpponent",
                        Triggers::creaturesYouControlDealCombatDamageToOpponent, CardType.EMBLEM,
                        (test, cards) -> {
                            for (Card card : cards) {
                                test.gd.emblems.add(new Emblem(test.player1.getId(), List.of(), card, List.of(),
                                        test.gd.nextTimestamp(), card.getTriggeredAbilities()));
                            }
                            test.addCreatureReady(test.player1, new GrizzlyBears()).setAttacking(true);
                            test.harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> test.resolveCombat());
                        }),
                watch("a creature deals combat damage to its owner", "creatureDealsCombatDamageToItsOwner",
                        Triggers::creatureDealsCombatDamageToItsOwner, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            GrizzlyBears stolen = new GrizzlyBears();
                            stolen.setOwnerId(test.player2.getId());
                            test.addCreatureReady(test.player1, stolen).setAttacking(true);
                            test.harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> test.resolveCombat());
                        }),
                watch("a creature you control deals combat damage to a player or planeswalker",
                        "creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker",
                        Triggers::creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker, CardType.CREATURE,
                        (test, cards) -> {
                            test.gd.playerGraveyards.get(test.player1.getId()).addAll(cards);
                            test.addCreatureReady(test.player1, new GrizzlyBears()).setAttacking(true);
                            test.harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> test.resolveCombat());
                        }).in(AbilityZones.GRAVEYARD),
                watch("you or your planeswalker is dealt combat damage", "youOrYourPlaneswalkerDealtCombatDamage",
                        Triggers::youOrYourPlaneswalkerDealtCombatDamage, CardType.CREATURE,
                        (test, cards) -> {
                            test.gd.playerGraveyards.get(test.player1.getId()).addAll(cards);
                            test.addCreatureReady(test.player2, new GrizzlyBears()).setAttacking(true);
                            test.harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                                    () -> test.resolveCombat(test.player2));
                        }).in(AbilityZones.GRAVEYARD),
                watch("enchanted player is dealt damage", "enchantedPlayerDealtDamage",
                        Triggers::enchantedPlayerDealtDamage, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            for (Card card : cards) {
                                card.setEnchantPlayer(true);
                                test.harness.addToBattlefieldAndReturn(test.player1, card)
                                        .setAttachedTo(test.player2.getId());
                            }
                            test.rememberPermanents();
                            test.helperEntersAndItsAbilityResolves(
                                    new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
                        }, CardSubtype.AURA),
                watch("enchanted creature deals damage to you", "enchantedCreatureDealsDamageToYou",
                        Triggers::enchantedCreatureDealsDamageToYou, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            Permanent attacker = test.addCreatureReady(test.player2, new GrizzlyBears());
                            attacker.setAttacking(true);
                            for (Card card : cards) {
                                test.addCreatureReady(test.player1, card).setAttachedTo(attacker.getId());
                            }
                            test.rememberPermanents();
                            test.harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                                    () -> test.resolveCombat(test.player2));
                        }, CardSubtype.AURA),
                watch("enchanted creature deals damage to a creature", "enchantedCreatureDealsDamageToCreature",
                        Triggers::enchantedCreatureDealsDamageToCreature, CardType.ENCHANTMENT,
                        TriggerRouteRecordTest::attachedCreatureDealsCombatDamageToAWall, CardSubtype.AURA),
                watch("equipped creature deals damage to a creature", "equippedCreatureDealsDamageToCreature",
                        Triggers::equippedCreatureDealsDamageToCreature, CardType.ARTIFACT,
                        TriggerRouteRecordTest::attachedCreatureDealsCombatDamageToAWall, CardSubtype.EQUIPMENT),
                watch("an opponent is dealt damage by a red instant or sorcery spell you control", "opponentDealtDamageByRedInstantSorceryOrPlaneswalkerYouControl", Triggers::opponentDealtDamageByRedInstantSorceryOrPlaneswalkerYouControl, CardType.CREATURE,
                        (test, cards) -> {
                            test.gd.playerGraveyards.get(test.player1.getId()).addAll(cards);
                            test.youShock(test.player2.getId());
                        }).in(AbilityZones.GRAVEYARD),
                watch("an instant or sorcery spell you control deals damage to an opponent or battle", "instantOrSorceryYouControlDealsDamageToOpponentOrBattle", Triggers::instantOrSorceryYouControlDealsDamageToOpponentOrBattle, CardType.CREATURE,
                        (test, cards) -> {
                            test.gd.playerGraveyards.get(test.player1.getId()).addAll(cards);
                            test.youShock(test.player2.getId());
                        }).in(AbilityZones.GRAVEYARD),
                watch("an instant or sorcery spell you control deals damage to an opponent", "instantOrSorceryYouControlDealsDamageToOpponent", Triggers::instantOrSorceryYouControlDealsDamageToOpponent, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.youShock(test.player2.getId());
                        }),
                watch("an instant or sorcery spell you control deals damage to a player", "instantOrSorceryYouControlDealsDamageToPlayer", Triggers::instantOrSorceryYouControlDealsDamageToPlayer, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.youShock(test.player2.getId());
                        }),
                watch("an instant or sorcery spell you control deals damage", "instantOrSorceryYouControlDealsDamage", Triggers::instantOrSorceryYouControlDealsDamage, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.youShock(test.player2.getId());
                        }),
                watch("an instant or sorcery spell you control deals damage to its only target creature", "instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature", Triggers::instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.youShock(test.addCreatureReady(test.player2, card("Victim", CardType.CREATURE)).getId());
                        }),
                watch("a creature you control deals damage to a planeswalker", "creatureYouControlDealsDamageToPlaneswalker", Triggers::creatureYouControlDealsDamageToPlaneswalker, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.creatureDealsCombatDamageToPlaneswalker(test.opponentPlaneswalker());
                        }),
                watch("a creature you control deals combat damage to a planeswalker", "creatureYouControlDealsCombatDamageToPlaneswalker", Triggers::creatureYouControlDealsCombatDamageToPlaneswalker, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            test.fixturePermanents(cards);
                            test.creatureDealsCombatDamageToPlaneswalker(test.opponentPlaneswalker());
                        }),
                watch("a creature deals damage to enchanted planeswalker", "creatureDealsDamageToEnchantedPlaneswalker",
                        Triggers::creatureDealsDamageToEnchantedPlaneswalker, CardType.ENCHANTMENT,
                        (test, cards) -> {
                            Permanent walker = test.opponentPlaneswalker();
                            for (Card card : cards) {
                                test.addCreatureReady(test.player1, card).setAttachedTo(walker.getId());
                            }
                            test.rememberPermanents();
                            test.creatureDealsCombatDamageToPlaneswalker(walker);
                        }, CardSubtype.AURA));
    }

    /**
     * A watch on an enchantment of player1 whose event the engine reports by calling {@code collector} on
     * {@link TriggerCollectionService}: the scenario puts the fixtures on the battlefield and makes that call.
     */
    private static Watch collected(String name, String factory, Supplier<Trigger> trigger,
                                   BiConsumer<TriggerRouteRecordTest, TriggerCollectionService> collector) {
        return watch(name, factory, trigger, CardType.ENCHANTMENT, (test, cards) -> {
            test.fixturePermanents(cards);
            test.mainPhaseOf(test.player1);
            test.collect(collection -> collector.accept(test, collection));
        });
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

    /**
     * Every trigger a card can write, each method of {@link Triggers} that makes one, is measured by a watch or is in
     * {@link #UNMEASURED} with the reason it can't be: a new factory fails here until it has one of the two.
     */
    @Test
    void everyTriggerFactoryHasAWatchOrAReason() {
        Set<String> factories = Arrays.stream(Triggers.class.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers())
                        && Trigger.class.isAssignableFrom(method.getReturnType()))
                .map(Method::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> watched = watches().stream().map(Watch::factory).collect(Collectors.toCollection(TreeSet::new));

        assertThat(watched).as("watches on a factory Triggers doesn't have").isSubsetOf(factories);
        assertThat(UNMEASURED.keySet()).as("reasons for a factory Triggers doesn't have").isSubsetOf(factories);
        assertThat(UNMEASURED.keySet()).as("factories with a watch and a reason").doesNotContainAnyElementsOf(watched);
        Set<String> neither = new TreeSet<>(factories);
        neither.removeAll(watched);
        neither.removeAll(UNMEASURED.keySet());
        assertThat(neither).as("trigger factories with neither a watch nor a reason in UNMEASURED").isEmpty();
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

    /** Has the engine's trigger collection report an event, as the service that makes the event happen does. */
    private void collect(Consumer<TriggerCollectionService> collector) {
        TriggerCollectionService collection = bean(TriggerCollectionService.class);
        harness.inMutationScope(() -> collector.accept(collection));
    }

    /** The fixtures are cards in player1's graveyard, on top of the creature cards the graveyard target needs. */
    private void fixturesInGraveyard(List<Card> cards, Consumer<TriggerCollectionService> collector) {
        gd.playerGraveyards.get(player1.getId()).addAll(cards);
        mainPhaseOf(player1);
        collect(collector);
    }

    /** A creature of player1 untaps in player1's untap step, and the step finishes. */
    private void untapStepFinishes(List<Card> cards) {
        fixturePermanents(cards);
        addCreatureReady(player1, new GrizzlyBears()).tap();
        harness.performUntapStep(player1);
        UntapStepService untap = bean(UntapStepService.class);
        harness.inMutationScope(() -> untap.finishUntapStep(gd, player1.getId()));
    }

    /** A helper creature of player2 enters and its own enters ability, {@code effect}, resolves. */
    private void opponentHelperAbilityResolves(CardEffect effect) {
        Card helper = card("Helper", CardType.CREATURE);
        helper.addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        mainPhaseOf(player2);
        harness.enterBattlefieldAndReturn(player2, helper);
        harness.passBothPriorities();
    }

    /** Player2's helper scries or surveils and keeps the cards at {@code kept} on top. */
    private void opponentLooksAtLibrary(List<Card> cards, CardEffect effect, List<Integer> kept) {
        fixturePermanents(cards);
        opponentHelperAbilityResolves(effect);
        if (gd.interaction.isAwaitingInput()) {
            gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(kept, List.of(), null));
        }
    }

    private void librarySearched(List<Card> cards, Player searcher) {
        fixturePermanents(cards);
        mainPhaseOf(player1);
        GameLogService gameLog = bean(GameLogService.class);
        harness.inMutationScope(() ->
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gd, gameLog, searcher.getId()));
    }

    /** Each fixture is a Curse attached to player1, who draws a card. */
    private void enchantedPlayerDraws(List<Card> cards) {
        for (Card card : cards) {
            harness.addToBattlefieldAndReturn(player1, card).setAttachedTo(player1.getId());
        }
        rememberPermanents();
        helperEntersAndItsAbilityResolves(new DrawCardEffect(1));
    }

    private void cycleFromHand(Card cycler) {
        cycler.addCycling("{1}");
        mainPhaseOf(player1);
        harness.setHand(player1, List.of(cycler));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
    }

    private void youCycleACard(List<Card> cards) {
        fixturePermanents(cards);
        cycleFromHand(card("Cycler", CardType.INSTANT));
    }

    private void fixturesAreCycled(List<Card> cards) {
        for (Card card : cards) {
            cycleFromHand(card);
            answerAll();
        }
    }

    private void tapForMana(Player player, Permanent permanent) {
        mainPhaseOf(player);
        harness.tapPermanent(player, gd.playerBattlefields.get(player.getId()).indexOf(permanent));
    }

    private void fixturesAreTappedForMana(List<Card> cards) {
        cards.forEach(card -> card.addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN)));
        for (Permanent permanent : fixtureCreatures(cards)) {
            tapForMana(player1, permanent);
            answerAll();
        }
    }

    private void fixtureLandsAreTappedForMana(List<Card> cards) {
        cards.forEach(card -> card.addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN)));
        for (Card card : cards) {
            tapForMana(player1, harness.addToBattlefieldAndReturn(player1, card));
            answerAll();
        }
    }

    private void opponentTapsALandForMana(List<Card> cards) {
        fixturePermanents(cards);
        tapForMana(player2, harness.addToBattlefieldAndReturn(player2, new Forest()));
    }

    private void enchantedLandIsTappedForMana(List<Card> cards) {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        for (Card card : cards) {
            harness.addToBattlefieldAndReturn(player1, card).setAttachedTo(forest.getId());
        }
        rememberPermanents();
        tapForMana(player1, forest);
    }

    /** Player1 taps {@code source}, a nonland permanent given a mana ability, for mana. */
    private void youTapForMana(List<Card> cards, Card source) {
        fixturePermanents(cards);
        source.addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        tapForMana(player1, addCreatureReady(player1, source));
    }

    /** The helper's ability, controlled by player1, taps a Goblin of player2. */
    private void youTapAnOpponentCreature(List<Card> cards) {
        addCreatureReady(player2, card("Victim", CardType.CREATURE, CardSubtype.GOBLIN));
        helperAbilityResolves(cards, new TapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN))));
    }

    private void changeControl(Permanent permanent, Player newController) {
        CreatureControlService control = bean(CreatureControlService.class);
        harness.inMutationScope(() -> control.applyControlEffect(gd, newController.getId(), permanent,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT, null, "Helper"));
    }

    /** An Aura of player1 becomes attached to {@code enchanted}. */
    private void auraBecomesAttachedTo(Permanent enchanted) {
        Permanent aura = addCreatureReady(player1, card("Fixture Aura", CardType.ENCHANTMENT, CardSubtype.AURA));
        aura.setAttachedTo(enchanted.getId());
        collect(collection -> collection.checkAuraAttachedTriggers(gd, aura, enchanted.getId()));
    }

    private void fixtureEquipmentBecomesAttached(List<Card> cards) {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        for (Card card : cards) {
            Permanent equipment = addCreatureReady(player1, card);
            rememberPermanents();
            equipment.setAttachedTo(creature.getId());
            collect(collection -> collection.checkEquipmentAttachedTriggers(gd, equipment, null));
            answerAll();
        }
    }

    private void fixtureEquipmentBecomesUnattached(List<Card> cards) {
        UnattachTriggerSupport unattach = bean(UnattachTriggerSupport.class);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        for (Card card : cards) {
            Permanent equipment = addCreatureReady(player1, card);
            rememberPermanents();
            equipment.setAttachedTo(creature.getId());
            harness.inMutationScope(() ->
                    unattach.triggerDestroyOnUnattachIfNeeded(gd, equipment, creature.getId()));
            equipment.setAttachedTo(null);
            answerAll();
        }
    }

    /** Each fixture card is a face-up planar card of player1 whose {@code trigger} ability triggers. */
    private void planarAbilityTriggers(List<Card> cards, Trigger trigger) {
        PlanechaseService planar = bean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        mainPhaseOf(player1);
        for (Card card : cards) {
            PlanarObject plane = new PlanarObject(card, gd.nextTimestamp());
            gd.planechase.faceUp.add(plane);
            harness.inMutationScope(() -> planar.trigger(gd, plane, trigger, player1.getId()));
            answerAll();
        }
    }

    private void stateIsChecked(List<Card> cards) {
        fixturePermanents(cards);
        addCreatureReady(player1, new GrizzlyBears());
        mainPhaseOf(player1);
        StateTriggerService stateTriggers = bean(StateTriggerService.class);
        harness.inMutationScope(() -> stateTriggers.checkStateTriggers(gd));
    }

    private void spellIsCopied(List<Card> cards, Player copier) {
        fixturePermanents(cards);
        mainPhaseOf(player1);
        StackEntry copied = new StackEntry(StackEntryType.INSTANT_SPELL, card("Copied", CardType.INSTANT),
                copier.getId(), "Copied", List.of(new GainLifeEffect(1)));
        gd.stack.add(copied);
        collect(collection -> collection.checkSpellCopyTriggers(gd, copied));
    }

    /** A helper creature of {@code player} has its "{T}: You gain 1 life" activated. */
    private void abilityIsActivated(List<Card> cards, Player player) {
        fixturePermanents(cards);
        Card helper = card("Helper", CardType.CREATURE);
        helper.addActivatedAbility(
                new ActivatedAbility(true, null, List.of(new GainLifeEffect(1)), "{T}: You gain 1 life."));
        Permanent source = addCreatureReady(player, helper);
        mainPhaseOf(player);
        harness.activateAbility(player, gd.playerBattlefields.get(player.getId()).indexOf(source), null, null);
    }

    /**
     * Each fixture is a permanent of player1 whose own event the engine reports by calling {@code collector} with
     * the permanent.
     */
    private void fixturesAreCollected(List<Card> cards, BiConsumer<TriggerCollectionService, Permanent> collector) {
        mainPhaseOf(player1);
        for (Card card : cards) {
            Permanent permanent = addCreatureReady(player1, card);
            rememberPermanents();
            collect(collection -> collector.accept(collection, permanent));
            answerAll();
        }
    }

    private void helperExploits(List<Card> cards) {
        fixturePermanents(cards);
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        helperEntersAndItsAbilityResolves(new ExploitEffect());
        harness.handlePermanentChosen(player1, fodder.getId());
    }

    /** {@code creature} crews a Vehicle of player1 in player1's main phase; the crew ability stays on the stack. */
    private void crewAVehicleWith(Permanent creature) {
        Card vehicleCard = card("Fixture Vehicle", CardType.ARTIFACT, CardSubtype.VEHICLE);
        vehicleCard.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()), "Crew 1"));
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, vehicleCard);
        mainPhaseOf(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
    }

    private void fixturesCrewAVehicle(List<Card> cards) {
        for (Card card : cards) {
            Permanent creature = addCreatureReady(player1, card);
            rememberPermanents();
            crewAVehicleWith(creature);
            answerAll();
        }
    }

    /** Each fixture has a station ability, activated by tapping another creature of player1. */
    private void fixturesAreStationed(List<Card> cards) {
        for (Card card : cards) {
            card.addActivatedAbility(new ActivatedAbility(false, null,
                    List.of(new TapCreatureCost(new PermanentIsCreaturePredicate(), true, true, false, true),
                            new GainLifeEffect(1)), "Station"));
            Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, card);
            rememberPermanents();
            Permanent crew = addCreatureReady(player1, new GrizzlyBears());
            mainPhaseOf(player1);
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spacecraft),
                    null, null);
            if (gd.interaction.isAwaitingInput()) {
                harness.handlePermanentChosen(player1, crew.getId());
            }
            answerAll();
        }
    }

    /** How an effect handler resolves an effect of a stack entry. */
    @FunctionalInterface
    private interface Resolution<H, E extends CardEffect> {
        void resolve(H handler, GameData game, StackEntry entry, E effect);
    }

    /**
     * Each fixture's own ability with {@code effect} resolves, by its handler: the entry that resolves is not on the
     * stack, so only what the effect makes trigger is.
     */
    private <H, E extends CardEffect> void fixturesResolve(List<Card> cards, E effect, Class<H> handlerType,
                                                           Resolution<H, E> resolution) {
        H handler = bean(handlerType);
        mainPhaseOf(player1);
        for (Card card : cards) {
            Permanent permanent = addCreatureReady(player1, card);
            rememberPermanents();
            StackEntry resolving = new StackEntry(StackEntryType.ACTIVATED_ABILITY, card, player1.getId(),
                    "Fixture ability", new ArrayList<>(List.of(effect)), null, permanent.getId());
            harness.inMutationScope(() -> resolution.resolve(handler, gd, resolving, effect));
            answerAll();
        }
    }

    /** A counter of {@code kind} is removed from each of {@code permanents}, and the removal is noticed. */
    private void countersAreRemoved(List<Permanent> permanents, CounterType kind) {
        for (Permanent permanent : permanents) {
            permanent.setCounterCount(kind, 2);
            permanent.setCounterCount(kind, 1);
        }
        mainPhaseOf(player1);
        collect(collection -> collection.checkLoyaltyCounterRemovalTriggers(gd));
    }

    /** {@code placer} puts a +1/+1 counter on a creature of player2. */
    private void countersArePutOnOpponentCreature(List<Card> cards, Player placer) {
        fixturePermanents(cards);
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        mainPhaseOf(player1);
        PermanentCounterSupport counters = bean(PermanentCounterSupport.class);
        harness.inMutationScope(() -> counters.placeCounterOnPermanentForPlayer(gd, null, theirs,
                CounterType.PLUS_ONE_PLUS_ONE, 1, placer.getId()));
    }

    /** A helper creature of player1 with {@code subtype} enters and puts a +1/+1 counter on itself. */
    private void countersArePutOnHelper(List<Card> cards, CardSubtype subtype) {
        fixturePermanents(cards);
        Card helper = card("Helper", CardType.CREATURE, subtype);
        helper.addTrigger(Triggers.enters(ObjectRelation.THIS), new PutCountersOnSourceEffect(1, 1, 1));
        mainPhaseOf(player1);
        harness.enterBattlefieldAndReturn(player1, helper);
        harness.passBothPriorities();
    }

    private void timeCounterIsRemovedFromExiledFixtures(List<Card> cards) {
        RemoveTimeCounterFromExiledCardEffectHandler removal =
                bean(RemoveTimeCounterFromExiledCardEffectHandler.class);
        mainPhaseOf(player1);
        for (Card card : cards) {
            gd.addToExile(player1.getId(), card);
            gd.exiledCardTimeCounters.put(card.getId(), 2);
            harness.inMutationScope(() -> removal.removeTimeCounter(gd, card.getId()));
            answerAll();
        }
    }

    /** Each fixture is a face-up plane; a creature of player2 deals damage to player2. */
    private void opponentCreatureDamagesItsController(List<Card> cards) {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        for (Card card : cards) {
            gd.planechase.faceUp.add(new PlanarObject(card, gd.nextTimestamp()));
        }
        opponentHelperAbilityResolves(new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }

    /** The helper's ability puts a -1/-1 counter on a Goblin of player1. */
    private void minusOneCounterIsPutOnACreature(List<Card> cards) {
        addCreatureReady(player1, card("Victim", CardType.CREATURE, CardSubtype.GOBLIN));
        helperAbilityResolves(cards, new PutCounterOnEachControlledPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN))));
    }

    /** Player2 casts an instant that deals 1 damage to {@code targetId}; the spell stays on the stack. */
    private void opponentCastsABoltAt(UUID targetId) {
        Card bolt = card("Bolt", CardType.INSTANT);
        bolt.addSpellEffect(new DealDamageToAnyTargetEffect(1));
        mainPhaseOf(player2);
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, targetId);
    }

    private void opponentTargets(List<Card> cards, UUID targetId) {
        fixturePermanents(cards);
        opponentCastsABoltAt(targetId);
    }

    private void fixturesBecomeOpponentSpellTargets(List<Card> cards) {
        for (Permanent permanent : fixtureCreatures(cards)) {
            opponentCastsABoltAt(permanent.getId());
            answerAll();
        }
    }

    private void fixturesBecomeAuraSpellTargets(List<Card> cards) {
        for (Permanent permanent : fixtureCreatures(cards)) {
            Card aura = card("Fixture Aura", CardType.ENCHANTMENT, CardSubtype.AURA);
            aura.setKeywords(Set.of(Keyword.FLASH));
            aura.target(TargetFilters.creature())
                    .addStaticEffect(new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE));
            mainPhaseOf(player1);
            harness.setHand(player1, List.of(aura));
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castEnchantment(player1, 0, permanent.getId());
            answerAll();
        }
    }

    /** A creature of player1 has its "{T}: This creature deals 1 damage to any target" activated at a target. */
    private void youActivateAPingAt(UUID targetId) {
        Card pinger = card("Pinger", CardType.CREATURE);
        pinger.addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This creature deals 1 damage to any target."));
        Permanent source = addCreatureReady(player1, pinger);
        mainPhaseOf(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, targetId);
    }

    private void backupAbilityTargetsYourCreature(List<Card> cards) {
        fixturePermanents(cards);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card backup = card("Backup", CardType.CREATURE);
        backup.setKeywords(Set.of(Keyword.BACKUP));
        backup.target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        mainPhaseOf(player1);
        harness.castFromHand(player1, backup, "{1}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void opponentDraws(List<Card> cards, int count) {
        fixturePermanents(cards);
        mainPhaseOf(player1);
        gd.cardsDrawnThisTurn.put(player2.getId(), 0);
        DrawService draw = bean(DrawService.class);
        harness.inMutationScope(() -> draw.resolveDrawCards(gd, player2.getId(), count));
    }

    private void removal(Consumer<PermanentRemovalService> move) {
        PermanentRemovalService removal = bean(PermanentRemovalService.class);
        mainPhaseOf(player1);
        harness.inMutationScope(() -> move.accept(removal));
    }

    /** Player1's {@code step} has begun and the step's abilities are collected. */
    private void stepIsHandled(List<Card> cards, TurnStep step, BiConsumer<StepTriggerService, GameData> handler) {
        fixturePermanents(cards);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();
        StepTriggerService steps = bean(StepTriggerService.class);
        harness.inMutationScope(() -> handler.accept(steps, gd));
    }

    private void fixturesEnterFromGraveyard(List<Card> cards) {
        BattlefieldEntryService entry = bean(BattlefieldEntryService.class);
        TriggerCollectionService collection = bean(TriggerCollectionService.class);
        for (Card card : cards) {
            Permanent permanent = new Permanent(card);
            permanent.setEnteredFromZone(Zone.GRAVEYARD);
            permanent.setEnteredFromGraveyardOwnerId(player1.getId());
            harness.inMutationScope(() -> {
                entry.putPermanentOntoBattlefield(gd, player1.getId(), permanent);
                entry.processCreatureETBEffects(gd, player1.getId(), card, null, false);
                collection.processNextTriggeredModalTrigger(gd);
            });
            answerAll();
        }
    }

    /** Player1 casts Shock at {@code targetId} and it resolves. */
    private void youShock(UUID targetId) {
        mainPhaseOf(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private Permanent opponentPlaneswalker() {
        Permanent walker = harness.addToBattlefieldAndReturn(player2, card("Walker", CardType.PLANESWALKER));
        walker.setCounterCount(CounterType.LOYALTY, 5);
        return walker;
    }

    private void creatureDealsCombatDamageToPlaneswalker(Permanent walker) {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(walker.getId());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    /** A creature of player2 is declared as an attacker of {@code attackedId}. */
    private void opponentAttacks(UUID attackedId) {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player2, List.of(index), Map.of(index, attackedId));
        });
    }

    private void opponentAttacksAThirdPlayer(List<Card> cards) {
        fixturePermanents(cards);
        UUID third = UUID.randomUUID();
        gd.playerIds.add(third);
        gd.orderedPlayerIds.add(third);
        gd.playerNames.add("Third");
        gd.playerIdToName.put(third, "Third");
        gd.playerBattlefields.put(third, new ArrayList<>());
        gd.playerDecks.put(third, new ArrayList<>());
        gd.playerHands.put(third, new ArrayList<>());
        gd.playerGraveyards.put(third, new ArrayList<>());
        gd.playerLifeTotals.put(third, 20);
        opponentAttacks(third);
    }

    private void fixturesEnlist(List<Card> cards) {
        cards.forEach(card -> card.setKeywords(Set.of(Keyword.ENLIST)));
        List<Permanent> attackers = fixtureCreatures(cards);
        List<Permanent> supporters = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (Permanent attacker : attackers) {
            supporters.add(addCreatureReady(player1, new GrizzlyBears()));
            indices.add(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        }
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, indices);
            for (Permanent supporter : supporters) {
                harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
            }
        });
        gd.stack.removeIf(entry -> entry.getChosenPermanentId() != null);
    }

    private void fixturesBlockTwo(List<Card> cards) {
        cards.forEach(card -> card.addStaticEffect(new GrantAdditionalBlockEffect(1)));
        List<Permanent> attackers = List.of(addCreatureReady(player2, new GrizzlyBears()),
                addCreatureReady(player2, new GrizzlyBears()));
        attackers.forEach(attacker -> attacker.setAttacking(true));
        List<BlockerAssignment> assignments = new ArrayList<>();
        for (Permanent blocker : fixtureCreatures(cards)) {
            for (Permanent attacker : attackers) {
                assignments.add(new BlockerAssignment(
                        gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
            }
        }
        harness.forceActivePlayer(player2);
        prepareDeclareBlockers(player2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player1, assignments));
    }

    /** The creature the fixtures are attached to is blocked by a 0/4 and deals it combat damage. */
    private void attachedCreatureDealsCombatDamageToAWall(List<Card> cards) {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        for (Card card : cards) {
            addCreatureReady(player1, card).setAttachedTo(attacker.getId());
        }
        rememberPermanents();
        Card wall = card("Wall", CardType.CREATURE);
        wall.setPower(0);
        Permanent blocker = addCreatureReady(player2, wall);
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
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

    private void opponentCreatureAttacks(List<Card> cards) {
        fixturePermanents(cards);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
    }

    private void opponentCreatureDealsCombatDamageToYou(List<Card> cards) {
        fixturePermanents(cards);
        addCreatureReady(player2, new GrizzlyBears()).setAttacking(true);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat(player2));
    }

    private void anotherCreatureAttacksUnblocked(List<Card> cards) {
        fixturePermanents(cards);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
    }

    private void anotherCreatureDealsCombatDamageToCreature(List<Card> cards) {
        anotherCreatureBecomesBlocked(cards);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    private void fixturesAreDealtNoncombatDamage(List<Card> cards) {
        fixtureCreatures(cards);
        helperEntersAndItsAbilityResolves(new MassDamageEffect(1));
    }

    /**
     * A 2/4 Goblin of player2 is dealt {@code damage} noncombat damage by the helper's ability, and no other creature
     * is: one damage event, with excess damage when {@code damage} is more than 4.
     */
    private void opponentVictimIsDealtDamage(List<Card> cards, int damage) {
        addCreatureReady(player2, card("Victim", CardType.CREATURE, CardSubtype.GOBLIN));
        helperAbilityResolves(cards, new MassDamageEffect(damage, false, false,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN))));
    }

    /** A creature of player1 is blocked by a 0/4 creature of player2, so only the attacker deals damage. */
    private void anotherCreatureDealsCombatDamageToAWall(List<Card> cards) {
        fixturePermanents(cards);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card wall = card("Wall", CardType.CREATURE);
        wall.setPower(0);
        Permanent blocker = addCreatureReady(player2, wall);
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    /** A prevention shield of 1 on player1 prevents the 1 damage the helper's ability deals them. */
    private void damageToYouIsPrevented(List<Card> cards) {
        gd.playerDamagePreventionShields.merge(player1.getId(), 1, Integer::sum);
        helperAbilityResolves(cards, new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }

    private void anotherPermanentUntaps(List<Card> cards) {
        fixturePermanents(cards);
        addCreatureReady(player1, new GrizzlyBears()).tap();
        harness.performUntapStep(player1);
    }

    private void yourCreatureIsDestroyed(List<Card> cards) {
        fixturePermanents(cards);
        destroy(addCreatureReady(player1, new GrizzlyBears()));
    }

    private void opponentCreatureEnters(List<Card> cards) {
        fixturePermanents(cards);
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void enchantedCreatureIsDealtCombatDamage(List<Card> cards) {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        for (Card card : cards) {
            addCreatureReady(player1, card).setAttachedTo(attacker.getId());
        }
        blockEach(List.of(attacker));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
    }

    private void enchantedCreatureAttacksUnblocked(List<Card> cards) {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        for (Card card : cards) {
            addCreatureReady(player1, card).setAttachedTo(attacker.getId());
        }
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
    }

    private void opponentUpkeepBegins(List<Card> cards) {
        fixturePermanents(cards);
        advanceToUpkeep(player2);
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
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
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
            if (!targetReplacesThatObject) {
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
