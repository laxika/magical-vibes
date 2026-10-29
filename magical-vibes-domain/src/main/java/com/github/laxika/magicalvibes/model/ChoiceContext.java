package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public sealed interface ChoiceContext {

    /** The affected player selects the mana ability used by a resolving land drain. */
    /**
     * @param delayedColorless the drained mana is lost and its controller adds that much {C} later
     *                         (Pygmy Hippo) instead of taking the mana (Drain Power)
     */
    record LandManaDrainAbilityChoice(StackEntry entry, UUID landId,
                                      List<Integer> abilityIndices, List<UUID> remainingLandIds,
                                      boolean delayedColorless)
            implements ChoiceContext {
        public LandManaDrainAbilityChoice {
            entry = new StackEntry(entry);
            abilityIndices = List.copyOf(abilityIndices);
            remainingLandIds = List.copyOf(remainingLandIds);
        }
    }


    /** Retains an exile cast while its controller chooses the types of counters to remove. */
    record ExileCastCounterTypeChoice(UUID exileCardId, Integer xValue, UUID targetId,
                                      List<UUID> counterPermanentIds, List<UUID> convokeCreatureIds,
                                      List<UUID> waterbendPermanentIds, boolean waterbendPaid,
                                      List<UUID> targetIds, boolean resolutionCast, boolean autoPass,
                                      boolean copy, boolean putOnBottom, boolean flashforwardCast,
                                      Zone sourceZoneOverride, boolean fromBattlefield,
                                      List<CounterType> chosenTypes) implements ChoiceContext {
        public ExileCastCounterTypeChoice {
            counterPermanentIds = List.copyOf(counterPermanentIds);
            convokeCreatureIds = List.copyOf(convokeCreatureIds);
            waterbendPermanentIds = List.copyOf(waterbendPermanentIds);
            targetIds = List.copyOf(targetIds);
            chosenTypes = List.copyOf(chosenTypes);
        }
    }

    /** A controller chooses which of their exiled permanents is replaced next. */
    record PermanentReplacementOrder(List<UUID> permanentIds) implements ChoiceContext {
        public PermanentReplacementOrder {
            permanentIds = List.copyOf(permanentIds);
        }
    }


    /** The affected player chooses one overlapping creature-damage prevention shield. */
    record CreaturePreventionLifeGainChoice(int preventedDamage, List<UUID> shieldControllers)
            implements ChoiceContext {
        public CreaturePreventionLifeGainChoice {
            shieldControllers = List.copyOf(shieldControllers);
        }
    }


    /** The resolving mutating spell's controller chooses its position in the merged creature. */
    record MutateOrderChoice(StackEntry entry) implements ChoiceContext {
        public MutateOrderChoice {
            entry = new StackEntry(entry);
        }
    }

    /** Retains an activation while its controller chooses how to pay a forage cost. */
    record ActivatedAbilityForageCostChoice(UUID playerId, PendingAbilityActivation activation)
            implements ChoiceContext {}

    /** Chooses whether to discard or pay mana for a prepared spell cast without its mana cost. */
    record FreeCastAdditionalCostChoice(StackEntry entry) implements ChoiceContext {
        public FreeCastAdditionalCostChoice {
            entry = new StackEntry(entry);
        }
    }

    /** Chooses between a card's shuffle replacement and an applicable graveyard exile replacement. */
    record GraveyardShuffleOrExileReplacementChoice(UUID ownerId, Card card) implements ChoiceContext {}

    /**
     * A discarded card with both its own madness ability and a granted one (CR 702.35a) is discarded
     * into exile under the madness cost its owner picks. {@code nativeCost} and {@code grantedCost} are
     * the two mana costs, in the order the choice offers them.
     */
    record MadnessCostChoice(UUID ownerId, Card card, String nativeCost, String grantedCost) implements ChoiceContext {}

    /**
     * A resolving instant or sorcery spell has two exile replacement effects (rebound and plot), so
     * its controller picks which one applies (CR 616.1). {@code card} is the spell's card, {@code physicalCard}
     * the card that actually leaves the stack.
     */
    record SpellReboundOrPlotChoice(UUID controllerId, UUID ownerId, Card card, Card physicalCard)
            implements ChoiceContext {}

    /**
     * The controller picks whether cards held out of every zone by a library look all go onto the
     * battlefield or all into hand (Green Sun's Twilight).
     */
    record HeldCardsZoneChoice(UUID controllerId, List<Card> cards) implements ChoiceContext {
        public HeldCardsZoneChoice {
            cards = List.copyOf(cards);
        }
    }

    /**
     * A resolving hand-cast instant or sorcery spell has two exile replacement effects (rebound and a
     * dream-counter exile such as Goliath Daydreamer's), so its controller picks which one applies
     * (CR 616.1). {@code card} is the spell's card, {@code physicalCard} the card that actually leaves
     * the stack.
     */
    record SpellReboundOrDreamCounterChoice(UUID controllerId, UUID ownerId, Card card, Card physicalCard)
            implements ChoiceContext {}

    /**
     * Several permanents each replace a resolving spell going to the graveyard with exiling it (Rod of
     * Absorption), so its controller picks which one applies (CR 616.1). The options are the
     * permanents' ids; {@code physicalCard} is the card that actually leaves the stack.
     */
    record SpellExileSourceChoice(UUID controllerId, UUID ownerId, Card card, Card physicalCard)
            implements ChoiceContext {}

    /** Divides one source's shared combat-damage prevention among its recipients. */
    record CombatSharedDamagePreventionChoice(UUID preventionSourceId, UUID damageSourceId,
                                             UUID controllerId, int remaining,
                                             List<UUID> recipients, Map<UUID, Integer> damage)
            implements ChoiceContext {
        public CombatSharedDamagePreventionChoice {
            recipients = List.copyOf(recipients);
            damage = Map.copyOf(damage);
        }
    }

    /** A separately applicable counter replacement, identified by its source. */
    record CounterReplacement(String label, CardEffect effect) {}

    /** Retains counter placement while the affected permanent's controller orders replacements. */
    record CounterReplacementOrder(StackEntry entry, UUID targetId, int count,
                                   List<CounterReplacement> remaining,
                                   com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect effect,
                                   List<UUID> subsequentTargets, int originalCount) implements ChoiceContext {
        public CounterReplacementOrder {
            remaining = List.copyOf(remaining);
            subsequentTargets = List.copyOf(subsequentTargets);
        }
    }

    /**
     * A counter placement on an entering permanent that was applied with the default replacement
     * order, although the order of {@code replacements} changes how many counters it enters with.
     * {@code placedCount} is the number the default order put on it.
     */
    record EntryCounterPlacement(CounterType type, int count, int placedCount,
                                 List<CounterReplacement> replacements) {
        public EntryCounterPlacement {
            replacements = List.copyOf(replacements);
        }
    }

    /**
     * Holds a permanent's battlefield entry, after its counters were placed but before it is on the
     * battlefield, while its controller orders the counter replacements of {@code placements}' first
     * placement (CR 616.1). {@code count} is that placement's count after the replacements already
     * chosen, and {@code remaining} the replacements still to order.
     */
    record EntryCounterReplacementOrder(BattlefieldEntryRequest request, UUID controllerId,
                                        Map<CounterType, Integer> countersBeforeEntry,
                                        com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect revealEffect,
                                        com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersWithCountersEffect revealWithCountersEffect,
                                        List<EntryCounterPlacement> placements, int count,
                                        List<CounterReplacement> remaining) implements ChoiceContext {
        public EntryCounterReplacementOrder {
            countersBeforeEntry = Map.copyOf(countersBeforeEntry);
            placements = List.copyOf(placements);
            remaining = List.copyOf(remaining);
        }
    }

    /**
     * One "if it would die, ... instead" replacement offered to a dying creature's controller.
     * Effects whose outcomes are identical are offered as a single {@link Kind#PLAIN} option.
     *
     * @param label              unique text shown to the choosing player
     * @param kind               what the replacement does instead of putting the creature into a graveyard
     * @param effect             the opponent-controlled replacement, for {@link Kind#OPPONENT}
     * @param sourceCard         the card generating the replacement, when it matters to the outcome
     * @param sourceControllerId the player controlling that card, for {@link Kind#OPPONENT}
     * @param sourcePermanentId  the permanent generating the replacement, when it matters to the outcome
     */
    record DeathExileReplacement(String label, Kind kind,
                                 com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect effect,
                                 Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId) {

        public enum Kind {
            /** Another replacement returns it to its owner's hand (Firestorm Phoenix). */
            RETURN_TO_HAND,
            /** Its own replacement puts it into its owner's library (Gravebane Zombie). */
            LIBRARY,
            /** Its card is revealed and put on the bottom of its owner's library (Wheel of Sun and Moon). */
            BOTTOM_OF_LIBRARY,
            /** Its own replacement reveals it and shuffles it into its owner's library (Progenitus). */
            SHUFFLE_INTO_LIBRARY,
            /** Exiles the creature with no further effect. */
            PLAIN,
            /** An opponent's replacement with a rider (Kalitas's Zombie, Gisa's tracking, ...). */
            OPPONENT,
            /** A spell's "exile it instead" that links the exiled card to its source. */
            TRACKED,
            /** A permanent's graveyard replacement that exiles the card with it (Void Maw, Valgavoth). */
            EXILED_WITH_PERMANENT,
            /** Exiles the card with a blood counter. */
            BLOOD_COUNTER,
            /** An opponent's graveyard replacement exiles the card with a void counter (Dauthi Voidwalker). */
            VOID_COUNTER,
            /** A face-up plane's exile replacement that also costs its controller life. */
            PLANAR,
            /** Exiles the permanent and returns it at the beginning of the next end step. */
            RETURN_AT_END_STEP,
            /** Its own replacement exiles it with egg counters (Darigaaz Reincarnated). */
            EGG_COUNTERS,
            /** Its own replacement exiles it and gives its controller an extra turn. */
            EXTRA_TURN,
            /** Its controller declined its only replacement, a "may" (Pulmonic Sliver); it dies normally. */
            NO_REPLACEMENT;

            /** Whether choosing it leaves the card in exile, so only its rider depends on the choice. */
            public boolean exiles() {
                return this != RETURN_TO_HAND && this != LIBRARY && this != BOTTOM_OF_LIBRARY
                        && this != SHUFFLE_INTO_LIBRARY && this != NO_REPLACEMENT;
            }
        }
    }

    /**
     * CR 616.1: the controller of the dying creature {@code permanentId} picks which of
     * {@code options} applies before the creature leaves the battlefield. For a creature of a departure
     * batch ({@code inDepartureBatch}) the choice is recorded on the pending batch, which is performed
     * once every such choice is made. Otherwise it was asked before the resolving effect or choice
     * completion changed anything: the choice is recorded and the effect is resolved again, its death
     * consuming the choice.
     */
    record DeathExileReplacementChoice(UUID permanentId, List<DeathExileReplacement> options,
                                       boolean inDepartureBatch) implements ChoiceContext {
        public DeathExileReplacementChoice {
            options = List.copyOf(options);
        }
    }

    /**
     * CR 616.1: the event's chooser picks which of {@code options} — the replacement and/or
     * prevention effects that currently apply to {@code event} — to apply next. {@code previews}
     * summarizes, per option, what choosing it can still lead to (empty when not computed).
     */
    record ReplacementEffectOrder(com.github.laxika.magicalvibes.model.replacement.ReplacementEvent event,
                                  List<com.github.laxika.magicalvibes.model.replacement.ReplacementOption> options,
                                  List<com.github.laxika.magicalvibes.model.replacement.ReplacementOptionPreview> previews)
            implements ChoiceContext {
        public ReplacementEffectOrder {
            options = List.copyOf(options);
            previews = List.copyOf(previews);
        }

        public ReplacementEffectOrder(com.github.laxika.magicalvibes.model.replacement.ReplacementEvent event,
                                      List<com.github.laxika.magicalvibes.model.replacement.ReplacementOption> options) {
            this(event, options, List.of());
        }
    }

    /** Chooses a legal front or Adventure spell face for a cast granted during resolution. */
    record ExileFreeCastFaceChoice(UUID exileCardId, Map<String, Integer> faces,
                                  boolean grantHaste, boolean returnToHandIfUnable,
                                  boolean suspendHaste, boolean completeInput) implements ChoiceContext {
        public ExileFreeCastFaceChoice {
            faces = Map.copyOf(faces);
        }
    }

    /** Chooses the mode of a modal spell cast from the top of a library during resolution (Galvanoth). */
    record LibraryFreeCastModeChoice(UUID cardId, Map<String, Integer> modes) implements ChoiceContext {
        public LibraryFreeCastModeChoice {
            modes = Map.copyOf(modes);
        }
    }

    /**
     * Chooses the mode of a modal spell cast from a graveyard during resolution (Quistis Trepe). {@code castEffect}
     * is the graveyard-cast permission being used and {@code chosenX} the X already announced for the cast, if any.
     */
    record GraveyardCastModeChoice(PendingMayAbility ability,
                                   CastCardFromGraveyardEffect castEffect,
                                   Integer chosenX, Map<String, Integer> modes) implements ChoiceContext {
        public GraveyardCastModeChoice {
            modes = Map.copyOf(modes);
        }
    }

    /** Chooses the mode of an exiled copy of a modal card as it is cast during resolution (Reenact the Crime). */
    record ExileCopyCastModeChoice(PendingMayAbility ability, Map<String, Integer> modes) implements ChoiceContext {
        public ExileCopyCastModeChoice {
            modes = Map.copyOf(modes);
        }
    }

    /** Adds chosen fixed-color mana before resolving the remaining effects of a mana ability. */
    record FixedManaColorThenEffectsChoice(UUID recipientPlayerId, List<ManaColor> colors,
                                           int amount, StackEntry followUp) implements ChoiceContext {
        public FixedManaColorThenEffectsChoice {
            colors = List.copyOf(colors);
            followUp = new StackEntry(followUp);
        }
    }

    /** Orders triggered abilities a player put on the stack together, from the bottom of the stack upward. */
    record TriggerOrder(List<UUID> remainingIds, List<UUID> orderedIds) implements ChoiceContext {
        public TriggerOrder {
            remainingIds = List.copyOf(remainingIds);
            orderedIds = List.copyOf(orderedIds);
        }
    }

    /** Chooses the result of conflicting tapped and untapped entry replacements. */
    record TappedEntryStateChoice(BattlefieldEntryRequest request) implements ChoiceContext {
        public TappedEntryStateChoice {
            request = request.deepCopy();
        }
    }

    /**
     * Orders several "enters as a copy of ..." replacement effects (CR 616.1). {@code sourceIds} lines up
     * with the prompt's options: the chosen source is the effect applied last, so its copy is the result.
     */
    record EnterAsCopyOrderChoice(BattlefieldEntryRequest request, List<UUID> sourceIds) implements ChoiceContext {
        public EnterAsCopyOrderChoice {
            request = request.deepCopy();
            sourceIds = List.copyOf(sourceIds);
        }
    }

    /** Positions an entering creature, or an existing creature whose controller has changed. */
    record CreaturePositionChoice(BattlefieldEntryRequest request, UUID permanentId,
                                  List<String> options) implements ChoiceContext {
        public CreaturePositionChoice {
            request = request == null ? null : request.deepCopy();
            options = List.copyOf(options);
        }
    }

    /** Chooses between the printed creature mana cost and its morph cost for a manifested or cloaked card. */
    record TurnFaceUpCostChoice(UUID permanentId) implements ChoiceContext {}

    /** The controller chooses a dungeon or an outgoing room while resolving venture. */
    record VentureChoice(StackEntry sourceEntry, Dungeon dungeon, Map<String, Integer> rooms)
            implements ChoiceContext {
        public VentureChoice {
            sourceEntry = new StackEntry(sourceEntry);
            rooms = Map.copyOf(rooms);
        }
    }

    /**
     * Chooses the destruction replacement (CR 614.8) that saves {@code permanentId}, a permanent of a departure
     * batch. It is asked in the batch's gather stage, before anything leaves, and recorded on the pending batch,
     * which applies it once every question of the batch is answered.
     */
    record RegenerationShieldChoice(UUID permanentId, Map<String, String> shields) implements ChoiceContext {
        public RegenerationShieldChoice {
            shields = Map.copyOf(shields);
        }
    }

    record CappedCounterAmountChoice(UUID sourcePermanentId) implements ChoiceContext {}

    /** Mana choice for Path of Ancestry, retaining the producing permanent for its cast trigger. */
    record PathOfAncestryManaColorChoice(UUID playerId, UUID sourcePermanentId, int amount)
            implements ChoiceContext {}

    record SagaChapterCounterAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId, String chapterName, CounterType counterType,
                                         List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                         int nextTargetIndex) implements ChoiceContext {

        public SagaChapterCounterAssignment {
            effects = List.copyOf(effects);
            targetIds = List.copyOf(targetIds);
            assignments = Map.copyOf(new java.util.LinkedHashMap<>(assignments));
        }
    }

    /**
     * Division before a spell or triggered ability is stacked; a null counter type denotes damage.
     * A non-null {@code stackEntryId} names a triggered ability already on the stack whose division
     * is being announced before any player receives priority; the answer is recorded on that entry
     * instead of pushing a new one. {@code entryTargetIds} and {@code entryTargetGroupSizes}, when
     * set, are every target of an ETB ability whose counters are divided among only one of its target
     * groups (Rhino, Terrible Trampler also destroys a target land), so the ability's stack entry
     * keeps all of its targets while only {@code targetIds} receive counters.
     */
    record CounterDistributionAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, CounterType counterType,
                                          List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                          int nextTargetIndex, boolean allowsPartialDistribution,
                                          PermanentChoiceContext.HandCastSpellTarget spellCast,
                                          UUID stackEntryId, List<UUID> entryTargetIds,
                                          List<Integer> entryTargetGroupSizes) implements ChoiceContext {

        public CounterDistributionAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                              UUID sourcePermanentId, CounterType counterType,
                                              List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                              int nextTargetIndex, boolean allowsPartialDistribution,
                                              PermanentChoiceContext.HandCastSpellTarget spellCast,
                                              UUID stackEntryId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, counterType, targetIds, assignments,
                    total, nextTargetIndex, allowsPartialDistribution, spellCast, stackEntryId, null, null);
        }

        public CounterDistributionAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                              UUID sourcePermanentId, CounterType counterType,
                                              List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                              int nextTargetIndex, boolean allowsPartialDistribution,
                                              PermanentChoiceContext.HandCastSpellTarget spellCast) {
            this(sourceCard, controllerId, effects, sourcePermanentId, counterType, targetIds, assignments,
                    total, nextTargetIndex, allowsPartialDistribution, spellCast, null);
        }

        public CounterDistributionAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                              UUID sourcePermanentId, CounterType counterType,
                                              List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                              int nextTargetIndex, boolean allowsPartialDistribution) {
            this(sourceCard, controllerId, effects, sourcePermanentId, counterType, targetIds, assignments,
                    total, nextTargetIndex, allowsPartialDistribution, null);
        }

        public CounterDistributionAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                              UUID sourcePermanentId, CounterType counterType,
                                              List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                              int nextTargetIndex) {
            this(sourceCard, controllerId, effects, sourcePermanentId, counterType, targetIds, assignments,
                    total, nextTargetIndex, false);
        }

        public CounterDistributionAssignment {
            effects = List.copyOf(effects);
            targetIds = List.copyOf(targetIds);
            assignments = Map.copyOf(new java.util.LinkedHashMap<>(assignments));
            entryTargetIds = entryTargetIds == null ? null : List.copyOf(entryTargetIds);
            entryTargetGroupSizes = entryTargetGroupSizes == null ? null : List.copyOf(entryTargetGroupSizes);
        }

        /**
         * The same prompt advanced to another target of the division, keeping the targets and target
         * group sizes of the whole ability.
         */
        public CounterDistributionAssignment withProgress(Map<UUID, Integer> newAssignments, int newNextTargetIndex) {
            return new CounterDistributionAssignment(sourceCard, controllerId, effects, sourcePermanentId,
                    counterType, targetIds, newAssignments, total, newNextTargetIndex, allowsPartialDistribution,
                    spellCast, stackEntryId, entryTargetIds, entryTargetGroupSizes);
        }
    }

    record TextChangeFromWord(UUID targetId, boolean untilEndOfTurn,
                             CardSubtype excludedReplacementCreatureType,
                             CardSubtype fixedReplacementCreatureType) implements ChoiceContext {
        public TextChangeFromWord(UUID targetId, boolean untilEndOfTurn) {
            this(targetId, untilEndOfTurn, CardSubtype.WALL, null);
        }

        public TextChangeFromWord(UUID targetId, boolean untilEndOfTurn, CardSubtype excludedReplacementCreatureType) {
            this(targetId, untilEndOfTurn, excludedReplacementCreatureType, null);
        }
    }

    record TextChangeToWord(UUID targetId, String fromWord, boolean isColor, boolean untilEndOfTurn,
                            boolean isCreatureType, CardSubtype excludedReplacementCreatureType) implements ChoiceContext {

        public TextChangeToWord(UUID targetId, String fromWord, boolean isColor, boolean untilEndOfTurn,
                                boolean isCreatureType) {
            this(targetId, fromWord, isColor, untilEndOfTurn, isCreatureType, CardSubtype.WALL);
        }

        public TextChangeToWord(UUID targetId, String fromWord, boolean isColor, boolean untilEndOfTurn) {
            this(targetId, fromWord, isColor, untilEndOfTurn, false);
        }
    }

    record ManaColorSpellChoice(UUID playerId, int amount, Set<CardSubtype> subtypes,
                                boolean anyColorCombination) implements ChoiceContext {
        public ManaColorSpellChoice {
            subtypes = Set.copyOf(subtypes);
        }

        public ManaColorSpellChoice(UUID playerId, int amount, Set<CardSubtype> subtypes) {
            this(playerId, amount, subtypes, false);
        }

        public static ManaColorSpellChoice anyColorCombination(UUID playerId, int amount,
                                                                Set<CardSubtype> subtypes) {
            return new ManaColorSpellChoice(playerId, amount, subtypes, true);
        }
    }

    record CommanderCounterManaColorChoice(UUID playerId, int amount, UUID recipientPlayerId) implements ChoiceContext {
        public CommanderCounterManaColorChoice(UUID playerId, int amount) {
            this(playerId, amount, null);
        }
        public CommanderCounterManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new CommanderCounterManaColorChoice(playerId, amount, recipientPlayerId);
        }
    }

    record SingleColorSubtypeSpellOrAbilityManaChoice(UUID playerId, int amount,
                                                       CardSubtype subtype, boolean fromCreature,
                                                       UUID sourcePermanentId, UUID recipientPlayerId,
                                                       boolean fromSnowSource, boolean fromCaveSource,
                                                       boolean fromDesertSource)
            implements ChoiceContext {

        public SingleColorSubtypeSpellOrAbilityManaChoice(UUID playerId, int amount,
                                                            CardSubtype subtype, boolean fromCreature) {
            this(playerId, amount, subtype, fromCreature, null, null, false, false, false);
        }

        public SingleColorSubtypeSpellOrAbilityManaChoice withSourcePermanentId(UUID sourcePermanentId) {
            return new SingleColorSubtypeSpellOrAbilityManaChoice(playerId, amount, subtype, fromCreature,
                    sourcePermanentId, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }

        public SingleColorSubtypeSpellOrAbilityManaChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new SingleColorSubtypeSpellOrAbilityManaChoice(playerId, amount, subtype, fromCreature,
                    sourcePermanentId, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }

        public SingleColorSubtypeSpellOrAbilityManaChoice withSnowSource(boolean fromSnowSource) {
            return new SingleColorSubtypeSpellOrAbilityManaChoice(playerId, amount, subtype, fromCreature,
                    sourcePermanentId, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }

        public SingleColorSubtypeSpellOrAbilityManaChoice withCaveSource(boolean fromCaveSource) {
            return new SingleColorSubtypeSpellOrAbilityManaChoice(playerId, amount, subtype, fromCreature,
                    sourcePermanentId, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }

        public SingleColorSubtypeSpellOrAbilityManaChoice withDesertSource(boolean fromDesertSource) {
            return new SingleColorSubtypeSpellOrAbilityManaChoice(playerId, amount, subtype, fromCreature,
                    sourcePermanentId, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }
    }

    record RestrictedManaColorChoice(UUID playerId, int amount, boolean fromCreature,
                                     List<ManaColor> fixedColorOptions,
                                     ManaRestriction restriction, boolean sameColor,
                                     boolean replacementsApplied) implements ChoiceContext {
        public RestrictedManaColorChoice(UUID playerId, int amount, boolean fromCreature,
                                         List<ManaColor> fixedColorOptions,
                                         ManaRestriction restriction, boolean sameColor) {
            this(playerId, amount, fromCreature, fixedColorOptions, restriction, sameColor, false);
        }
        public RestrictedManaColorChoice(UUID playerId, int amount, boolean fromCreature,
                                         List<ManaColor> fixedColorOptions,
                                         ManaRestriction restriction) {
            this(playerId, amount, fromCreature, fixedColorOptions, restriction, false);
        }
        public RestrictedManaColorChoice(UUID playerId, boolean fromCreature, int amount, List<ManaColor> fixedColorOptions, ManaRestriction restriction) {
            this(playerId, amount, fromCreature, fixedColorOptions, restriction, false);
        }

        public RestrictedManaColorChoice {
            fixedColorOptions = List.copyOf(fixedColorOptions);
        }

        /** The chosen color is the land's final mana; its replacement effects were already applied. */
        public RestrictedManaColorChoice withReplacementsApplied() {
            return new RestrictedManaColorChoice(playerId, amount, fromCreature, fixedColorOptions, restriction,
                    sameColor, true);
        }

    }

    record KickedSpellManaColorChoice(UUID playerId, int amount) implements ChoiceContext {}

    record PersistentManaColorChoice(UUID playerId, int amount) implements ChoiceContext {}
    record PersistentSpellOnlyManaColorChoice(UUID playerId, int amount, boolean anyColorCombination)
            implements ChoiceContext {}
    record TreasureManaColorChoice(UUID playerId, int amount) implements ChoiceContext {}
    record SourceTrackedManaColorChoice(UUID playerId, UUID sourcePermanentId, UUID recipientPlayerId,
                                        boolean fromCreature, int amount, boolean fromSnowSource,
                                        boolean fromCaveSource) implements ChoiceContext {}
    record NonHumanCreatureCounterManaColorChoice(UUID playerId, boolean fromCreature, int amount)
            implements ChoiceContext {}
    record CreatureCounterManaColorChoice(UUID playerId, boolean fromCreature, int amount)
            implements ChoiceContext {}
    record ExiledSpellManaColorChoice(UUID playerId, boolean fromCreature, int amount)
            implements ChoiceContext {}
    record GraveyardManaColorChoice(UUID playerId, boolean fromCreature, int amount) implements ChoiceContext {}
    record OutsideStartingDeckSpellManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                                    boolean anyColorCombination) implements ChoiceContext {}
    record CommanderManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                    UUID recipientPlayerId) implements ChoiceContext {

        public CommanderManaColorChoice(UUID playerId, boolean fromCreature, int amount) {
            this(playerId, fromCreature, amount, null);
        }

        public CommanderManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new CommanderManaColorChoice(playerId, fromCreature, amount, recipientPlayerId);
        }
    }
    record CommanderIdentityManaColorChoice(UUID playerId, int amount,
                                            List<ManaColor> fixedColorOptions) implements ChoiceContext {
        public CommanderIdentityManaColorChoice {
            fixedColorOptions = List.copyOf(fixedColorOptions);
        }
    }
    /**
     * @param replacementsApplied the mana's type is final: replacement effects that change it were
     *                            already applied, so the chosen color is added as is
     */
    record SpellOnlyManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                    boolean anyColorCombination, UUID recipientPlayerId,
                                    boolean replacementsApplied)
            implements ChoiceContext {

        public SpellOnlyManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                        boolean anyColorCombination) {
            this(playerId, fromCreature, amount, anyColorCombination, null, false);
        }

        public SpellOnlyManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                        boolean anyColorCombination, UUID recipientPlayerId) {
            this(playerId, fromCreature, amount, anyColorCombination, recipientPlayerId, false);
        }

        public SpellOnlyManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new SpellOnlyManaColorChoice(playerId, fromCreature, amount,
                    anyColorCombination, recipientPlayerId, replacementsApplied);
        }

        public SpellOnlyManaColorChoice withReplacementsApplied() {
            return new SpellOnlyManaColorChoice(playerId, fromCreature, amount,
                    anyColorCombination, recipientPlayerId, true);
        }
    }

    record MulticoloredSpellManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                             boolean anyColorCombination, UUID recipientPlayerId,
                                             boolean fromSnowSource, boolean fromCaveSource,
                                             boolean fromDesertSource)
            implements ChoiceContext {

        public MulticoloredSpellManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                                boolean anyColorCombination) {
            this(playerId, fromCreature, amount, anyColorCombination, null, false, false, false);
        }

        public MulticoloredSpellManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new MulticoloredSpellManaColorChoice(playerId, fromCreature, amount,
                    anyColorCombination, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }

        public MulticoloredSpellManaColorChoice withSnowSource(boolean fromSnowSource) {
            return new MulticoloredSpellManaColorChoice(playerId, fromCreature, amount,
                    anyColorCombination, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }

        public MulticoloredSpellManaColorChoice withCaveSource(boolean fromCaveSource) {
            return new MulticoloredSpellManaColorChoice(playerId, fromCreature, amount,
                    anyColorCombination, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }

        public MulticoloredSpellManaColorChoice withDesertSource(boolean fromDesertSource) {
            return new MulticoloredSpellManaColorChoice(playerId, fromCreature, amount,
                    anyColorCombination, recipientPlayerId, fromSnowSource, fromCaveSource, fromDesertSource);
        }
    }

    record ChosenPlayerManaColorChoice(UUID playerId, UUID sourceControllerId, UUID recipientPlayerId,
                                       boolean fromCreature, int amount) implements ChoiceContext {}

    record CommanderCastCounterManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                               UUID sourcePermanentId, List<ManaColor> allowedColors)
            implements ChoiceContext {
        public CommanderCastCounterManaColorChoice {
            allowedColors = List.copyOf(allowedColors);
        }
    }

    record EnchantedManaCostChoice(UUID playerId, List<List<ManaCost.ManaProductionOption>> choices,
                                   boolean fromCreature) implements ChoiceContext {
        public EnchantedManaCostChoice {
            choices = choices.stream().map(List::copyOf).toList();
        }
    }

    record ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                           boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                           boolean creatureSourceSpellOrAbility,
                           List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                           boolean artifactSpellOrAbilityOnly,
                           boolean grantsUncounterable, boolean manaValueAtLeastFour,
                           boolean creatureSpellOrAbilityOnly,
                           UUID sourcePermanentId,
                           Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                           boolean abilityOnly,
                           UUID recipientPlayerId,
                           boolean grantsAdditionalPlusOneCounter,
                           boolean fromSnowSource,
                           boolean fromCaveSource,
                           boolean grantsRiot,
                           ManaRestriction.SubtypeOrPlaneswalkerSpells restrictedToSubtypeSpell,
                           boolean differentColors,
                           boolean planeswalkerSpellOnly,
                           boolean fromBasicLandSource,
                           boolean creatureSpellManaValueAtLeastFourOrXOnly, boolean fromTreasureSource,
                           boolean fromArtifactSource,
                           boolean tracksSourceForSpellCastTriggers,
                           boolean fromDesertSource,
                           boolean replacementsApplied) implements ChoiceContext {
        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                           boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                           boolean creatureSourceSpellOrAbility,
                           List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                           boolean artifactSpellOrAbilityOnly,
                           boolean grantsUncounterable, boolean manaValueAtLeastFour,
                           boolean creatureSpellOrAbilityOnly,
                           UUID sourcePermanentId,
                           Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                           boolean abilityOnly,
                           UUID recipientPlayerId,
                           boolean grantsAdditionalPlusOneCounter,
                           boolean fromSnowSource,
                           boolean fromCaveSource,
                           boolean grantsRiot,
                           ManaRestriction.SubtypeOrPlaneswalkerSpells restrictedToSubtypeSpell,
                           boolean differentColors,
                           boolean planeswalkerSpellOnly,
                           boolean fromBasicLandSource,
                           boolean creatureSpellManaValueAtLeastFourOrXOnly, boolean fromTreasureSource,
                           boolean fromArtifactSource) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot, restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly, fromBasicLandSource, creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                           boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                           boolean creatureSourceSpellOrAbility,
                           List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                           boolean artifactSpellOrAbilityOnly,
                           boolean grantsUncounterable, boolean manaValueAtLeastFour,
                           boolean creatureSpellOrAbilityOnly,
                           UUID sourcePermanentId,
                           Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                           boolean abilityOnly,
                           UUID recipientPlayerId,
                           boolean grantsAdditionalPlusOneCounter,
                           boolean fromSnowSource,
                           boolean fromCaveSource,
                           boolean grantsRiot,
                           ManaRestriction.SubtypeOrPlaneswalkerSpells restrictedToSubtypeSpell,
                           boolean differentColors,
                           boolean planeswalkerSpellOnly,
                           boolean fromBasicLandSource,
                           boolean creatureSpellManaValueAtLeastFourOrXOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot, restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly, fromBasicLandSource, creatureSpellManaValueAtLeastFourOrXOnly, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                               boolean abilityOnly, UUID recipientPlayerId,
                               boolean grantsAdditionalPlusOneCounter, boolean fromSnowSource,
                               boolean fromCaveSource, boolean grantsRiot,
                               ManaRestriction.SubtypeOrPlaneswalkerSpells restrictedToSubtypeSpell,
                               boolean differentColors, boolean planeswalkerSpellOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                    recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell, differentColors,
                    planeswalkerSpellOnly, false, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes, boolean abilityOnly,
                               UUID recipientPlayerId, boolean grantsAdditionalPlusOneCounter,
                               boolean fromSnowSource, boolean grantsRiot) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                     sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                     recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource, false, grantsRiot,
                     null, false, false);
         }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes, boolean abilityOnly,
                               UUID recipientPlayerId, boolean grantsAdditionalPlusOneCounter,
                               boolean fromSnowSource) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                    recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    false, false, null, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                               boolean abilityOnly, UUID recipientPlayerId,
                               boolean grantsAdditionalPlusOneCounter) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                    recipientPlayerId, grantsAdditionalPlusOneCounter, false,
                    false, false, null, false, false);
        }

        /**
         * Marks the mana's type as final: replacement effects that change it, such as "colored mana
         * becomes white", were already applied (CR 616.1), so the chosen color is added as is.
         */
        public ManaColorChoice withReplacementsApplied() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource,
                    tracksSourceForSpellCastTriggers, fromDesertSource, true);
        }

        public ManaColorChoice withAmount(int amount) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource,
                    tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withSnowSource(boolean fromSnowSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withTreasureSource(boolean fromTreasureSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withArtifactSource(boolean fromArtifactSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withCaveSource(boolean fromCaveSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withDesertSource(boolean fromDesertSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource,
                    tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                               boolean artifactSpellOrAbilityOnly, boolean grantsUncounterable,
                               boolean manaValueAtLeastFour, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes, boolean abilityOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, false, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                               boolean artifactSpellOrAbilityOnly, boolean grantsUncounterable,
                               boolean manaValueAtLeastFour, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, false, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, false, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                               boolean grantsAdditionalPlusOneCounter) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, false, null,
                    grantsAdditionalPlusOneCounter);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                               boolean abilityOnly, UUID recipientPlayerId) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                    recipientPlayerId, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                               boolean artifactSpellOrAbilityOnly, boolean grantsUncounterable,
                               boolean manaValueAtLeastFour, boolean creatureSpellOrAbilityOnly,
                               UUID sourcePermanentId) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly,
                    false, false, false, false, null, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, null, false, false, false, false, false, null, null, false);
        }

        public ManaColorChoice withSourcePermanentId(UUID sourcePermanentId) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions,
                    creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly,
                    fromBasicLandSource, creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withSourceTracking() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, true, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withAdditionalPlusOneCounter() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId, true,
                    fromSnowSource, fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withRiot() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, true,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly,
                    fromBasicLandSource, creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions,
                    creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly,
                    fromBasicLandSource, creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withPlaneswalkerSpellOnly() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, true, fromBasicLandSource,
                    creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice withBasicLandSource(boolean fromBasicLandSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly,
                    fromBasicLandSource, creatureSpellManaValueAtLeastFourOrXOnly, fromTreasureSource, fromArtifactSource, tracksSourceForSpellCastTriggers, fromDesertSource, replacementsApplied);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature) {
            this(playerId, fromCreature, 1, null, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount) {
            this(playerId, fromCreature, amount, null, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly, boolean instantSorceryOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly, false);
        }

        /** "Add N mana of any one color, spendable only to cast instant/sorcery spells" (e.g. Resonating Lute). */
        public static ManaColorChoice instantSorceryOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, true, false);
        }

        /**
         * "Add N mana in any combination of colors, spendable only to cast spells of {@code subtype}
         * or activate abilities of permanents of that subtype" (e.g. Smokebraider). Each mana's color
         * is chosen individually (any combination).
         */
        public static ManaColorChoice subtypeSpellOrAbility(UUID playerId, int amount, CardSubtype subtype) {
            return new ManaColorChoice(playerId, false, amount, subtype, false, false, true);
        }

        public static ManaColorChoice subtypeSpellOrAbility(UUID playerId, int amount, Set<CardSubtype> subtypes) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, true,
                    false, null, false, false, false, false, false, null, subtypes, false);
        }

        public static ManaColorChoice subtypeSpellOnly(UUID playerId, int amount,
                                                        ManaRestriction.SubtypeOrPlaneswalkerSpells restriction) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, false,
                    false, null, false, false, false, false, false, null, null, false, null,
                    false, false, false, false, restriction, false, false);
        }

        /** "Add one mana of any color" restricted to the four party creature types. */
        public static ManaColorChoice partySpellOrAbility(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, true,
                    false, null, false, false, false, false, false, null,
                    Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD), false);
        }

        /**
         * "Add N mana of any color, spendable only to cast creature spells of the chosen type or
         * activate abilities of creature sources of the chosen type" (Secluded Courtyard).
         */
        public static ManaColorChoice creatureSourceSpellOrAbility(UUID playerId, int amount,
                                                                     CardSubtype subtype) {
            return new ManaColorChoice(playerId, false, amount, subtype, false, false, false,
                    true, null, false, false, false, false, false, null, null, false);
        }

        /**
         * "Add N mana, each chosen individually from a fixed list of colors" (filter lands such as
         * Fire-Lit Thicket's "Add {R}{R}, {R}{G}, or {G}{G}"). Each mana's color is picked separately
         * from {@code colors}, re-prompting until all {@code amount} have been chosen.
         */
        public static ManaColorChoice fixedColorCombination(UUID playerId, boolean fromCreature, int amount, List<ManaColor> colors) {
            return new ManaColorChoice(playerId, fromCreature, amount, null, false, false, false, colors, false);
        }

        /** "Add N mana in any combination of colors", spendable only to cast creature spells. */
        public static ManaColorChoice creatureSpellOnlyColorCombination(UUID playerId, boolean fromCreature,
                                                                         int amount, List<ManaColor> colors) {
            return new ManaColorChoice(
                    playerId, fromCreature, amount, null, false, false, false, false, colors,
                    true, false, false, false, false, null, null, false, null, false,
                    false, false, false, null, false, false);
        }

        public static ManaColorChoice riotColorCombination(UUID playerId, boolean fromCreature,
                                                            int amount, List<ManaColor> colors) {
            return fixedColorCombination(playerId, fromCreature, amount, colors).withRiot();
        }

        /** "Add N mana of different colors." Each subsequent pick excludes previous picks. */
        public static ManaColorChoice differentColors(UUID playerId, boolean fromCreature, int amount,
                                                       List<ManaColor> colors) {
            return new ManaColorChoice(playerId, fromCreature, amount, null, false, false, false,
                    false, colors, false, false, false, false, false, null, null, false, null,
                    false, false, false, false, null, true, false);
        }

        /** "Add N mana of different colors, spendable only to cast planeswalker spells." */
        public static ManaColorChoice planeswalkerSpellOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount).withPlaneswalkerSpellOnly();
        }

        /**
         * "Add N mana of any one color, spendable only to cast a creature spell of any type"
         * (Ancient Ziggurat). The color is chosen at activation; the mana routes to the pool's
         * creature-spell-only bucket.
         */
        public static ManaColorChoice creatureSpellOnly(UUID playerId, int amount) {
            return creatureSpellOnly(playerId, false, amount);
        }

        public static ManaColorChoice creatureSpellOnly(UUID playerId, boolean fromCreature, int amount) {
            return new ManaColorChoice(playerId, fromCreature, amount, null, false, false, false, null, true);
        }

        /** "Add N mana of any one color, spendable only to cast creature spells or activate abilities of creature sources" (Gwenna, Eyes of Gaea). */
        public static ManaColorChoice creatureSpellOrAbilityOnly(UUID playerId, int amount) {
            return creatureSpellOrAbilityOnly(playerId, false, amount);
        }

        public static ManaColorChoice creatureSpellOrAbilityOnly(UUID playerId, boolean fromCreature, int amount) {
            return new ManaColorChoice(
                    playerId, fromCreature, amount, null, false, false, false, false, null,
                    false, false, false, false, true, null, null, false, null);
        }

        /** "Add one mana of any of this creature's colors, spendable only to activate creature abilities." */
        public static ManaColorChoice creatureAbilityOnly(UUID playerId, boolean fromCreature, int amount,
                                                          List<ManaColor> colors) {
            return new ManaColorChoice(
                    playerId, fromCreature, amount, null, false, false, false, false, colors,
                    false, false, false, false, false, null, null, true, null);
        }

        /** "Add N mana of any one color, spendable only to activate abilities." */
        public static ManaColorChoice abilityOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, false, false,
                    null, false, false, false, false, false, null, null, true, null);
        }

        /** "Add N mana of any one color, spendable only to cast artifact spells or activate abilities of artifacts". */
        public static ManaColorChoice artifactSpellOrAbilityOnly(UUID playerId, int amount) {
            return new ManaColorChoice(
                    playerId, false, amount, null, false, false, false, false, null,
                    false, true, false, false, false, null, null, false);
        }

        /**
         * "Add one mana of any color, spendable only to cast a creature spell of the chosen type, and
         * that spell can't be countered" (Cavern of Souls). The mana routes to the pool's
         * subtype-creature bucket and is additionally marked as uncounterable-granting.
         */
        public static ManaColorChoice chosenSubtypeCreatureUncounterable(UUID playerId, int amount, CardSubtype subtype) {
            return new ManaColorChoice(
                    playerId, false, amount, subtype, false, false, false, false, null,
                    false, false, true, false, false, null, null, false);
        }

        /** "Add N mana of any one color, spendable only to cast spells with mana value 4 or greater." */
        public static ManaColorChoice manaValueAtLeastFour(UUID playerId, int amount) {
            return new ManaColorChoice(
                    playerId, false, amount, null, false, false, false, false, null,
                    false, false, false, true, false, null, null, false);
        }

        public static ManaColorChoice creatureSpellManaValueAtLeastFourOrXOnly(UUID playerId, int amount) {
            return new ManaColorChoice(
                    playerId, false, amount, null, false, false, false, false, null,
                    false, false, false, false, false, null, null, false, null, false,
                    false, false, false, null, false, false, false, true, false, false);
        }
    }

    record DifferentColorManaChoice(UUID playerId, int amount, ManaSpendRestriction restriction,
                                    ManaColor firstColor) implements ChoiceContext {}

    record CreatureAbilityManaColorChoice(UUID playerId, int amount, List<ManaColor> fixedColorOptions,
                                           UUID recipientPlayerId) implements ChoiceContext {
        public CreatureAbilityManaColorChoice(UUID playerId, int amount, List<ManaColor> fixedColorOptions) {
            this(playerId, amount, fixedColorOptions, null);
        }

        public CreatureAbilityManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new CreatureAbilityManaColorChoice(playerId, amount, fixedColorOptions, recipientPlayerId);
        }
    }

    /** A mana ability that adds mana equal to the chosen color's devotion. */
    record DevotionManaColorChoice(UUID playerId, UUID sourcePermanentId, boolean fromCreature,
                                   int manaMultiplier, boolean sourcePlanar) implements ChoiceContext {
        public DevotionManaColorChoice(UUID playerId, UUID sourcePermanentId, boolean fromCreature,
                                       int manaMultiplier) {
            this(playerId, sourcePermanentId, fromCreature, manaMultiplier, false);
        }
    }

    record DrawReplacementChoice(UUID playerId, DrawReplacementKind kind) implements ChoiceContext {}
    /**
     * The affected player picks which of several competing exile-instead-of-draw replacement effects
     * applies to one draw (CR 616.1); keys are the option labels, values the source permanents.
     */
    record DrawReplacementSourceChoice(UUID playerId, java.util.Map<String, UUID> sourcePermanentIds)
            implements ChoiceContext {
        public DrawReplacementSourceChoice {
            sourcePermanentIds = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(sourcePermanentIds));
        }
    }
    /**
     * The affected player chooses which of several Mangara's Tome replacements, each naming a different
     * exiled pile, applies to one draw (CR 616.1). {@code pileSourceIds} maps each option label to the
     * permanent the pile was exiled with.
     */
    record DrawFromExiledPileChoice(UUID playerId, java.util.Map<String, UUID> pileSourceIds) implements ChoiceContext {
        public DrawFromExiledPileChoice {
            pileSourceIds = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(pileSourceIds));
        }
    }

    /**
     * Krark's Thumb: the flipping player chooses which result of a replaced coin flip to keep when the
     * physical flips disagree. {@code coinIndex} is the logical flip's position in
     * {@link GameData#recordedCoinFlips}, and {@code playerId} is the player who flipped it.
     */
    record CoinFlipResultChoice(UUID playerId, int coinIndex) implements ChoiceContext {
        public static final String HEADS = "Heads";
        public static final String TAILS = "Tails";
    }

    /** The affected player orders competing delayed library-look replacements for one draw. */
    record DrawLookReplacementOrder(UUID playerId, List<Integer> counts) implements ChoiceContext {
        public DrawLookReplacementOrder {
            counts = List.copyOf(counts);
        }
    }

    record CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                          boolean nonbasicLandOnly, UUID attachedTo, CardType requiredType,
                          Zone landPlayZone, Permanent preparedPermanent) implements ChoiceContext {
        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                              boolean nonbasicLandOnly, UUID attachedTo, CardType requiredType, Zone landPlayZone) {
            this(card, controllerId, excludedTypes, nonbasicLandOnly, attachedTo, requiredType, landPlayZone, null);
        }

        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                              boolean nonbasicLandOnly, UUID attachedTo, CardType requiredType) {
            this(card, controllerId, excludedTypes, nonbasicLandOnly, attachedTo, requiredType, null);
        }

        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                              boolean nonbasicLandOnly, UUID attachedTo) {
            this(card, controllerId, excludedTypes, nonbasicLandOnly, attachedTo, null);
        }

        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes, CardType requiredType) {
            this(card, controllerId, excludedTypes, false, null, requiredType);
        }

        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes) {
            this(card, controllerId, excludedTypes, false, null);
        }

        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                              boolean nonbasicLandOnly) {
            this(card, controllerId, excludedTypes, nonbasicLandOnly, null);
        }
    }

    record CardTypeOnEnterChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                                 Permanent preparedPermanent) implements ChoiceContext {
        public CardTypeOnEnterChoice(Card card, UUID controllerId, List<CardType> excludedTypes) {
            this(card, controllerId, excludedTypes, null);
        }
    }

    record CraftedCardTypeOnEnterChoice(Permanent permanent, UUID controllerId,
                                         List<CardType> allowedTypes) implements ChoiceContext {}

    /**
     * "You and an opponent each choose a card name other than a basic land card name" as the source
     * enters (Null Chamber). {@code choosingPlayerId} is whoever is being asked right now:
     * {@code firstChosenName} is {@code null} on the controller's pick and holds it on the
     * opponent's follow-up pick, after which the permanent enters carrying both names.
     */
    record DualCardNameChoice(Card card, UUID controllerId, UUID choosingPlayerId,
                              String firstChosenName) implements ChoiceContext {}

    record KeywordGrantChoice(UUID targetId, List<Keyword> options,
                              GrantDuration duration, String sourceCardName,
                              UUID sourcePermanentId) implements ChoiceContext {
        public KeywordGrantChoice(UUID targetId, List<Keyword> options) {
            this(targetId, options, GrantDuration.END_OF_TURN, null, null);
        }
    }

    /** The owner of a Secret Mission conspiracy secretly chooses one of its missions before the game. */
    record SecretMissionChoice(Card sourceCard, List<String> missions) implements ChoiceContext {
        public SecretMissionChoice {
            missions = List.copyOf(missions);
        }
    }

    record LegacyWordChoice(Card sourceCard, List<String> options) implements ChoiceContext {
        public LegacyWordChoice {
            options = List.copyOf(options);
        }
    }

    /** Choosing a basic land type for a plain landwalk grant until end of turn. */
    record LandwalkGrantChoice(UUID targetId) implements ChoiceContext {}

    record ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes,
                              int maxCount, boolean drawForHandExiled,
                              com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                              String sourceSetCode, boolean findAllCards) implements ChoiceContext {

        public ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes,
                                 int maxCount, boolean drawForHandExiled,
                                 com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                 String sourceSetCode) {
            this(targetPlayerId, controllerId, excludedTypes, maxCount, drawForHandExiled, tokenTemplate,
                    sourceSetCode, false);
        }

        public ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes) {
            this(targetPlayerId, controllerId, excludedTypes, Integer.MAX_VALUE, false, null, null);
        }

        public ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes,
                                 boolean drawForHandExiled) {
            this(targetPlayerId, controllerId, excludedTypes, Integer.MAX_VALUE, drawForHandExiled, null, null);
        }

        public ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes,
                                 int maxCount, boolean drawForHandExiled) {
            this(targetPlayerId, controllerId, excludedTypes, maxCount, drawForHandExiled, null, null);
        }

        public boolean drawForEachHandCardExiled() {
            return drawForHandExiled;
        }
    }

    /**
     * @param followUpSubtypeChoice when non-null, the source's creature-subtype choice that is made right after the
     *                              name is chosen (an "as this becomes attached" choice pair that is not put on
     *                              the stack)
     */
    record ChooseCardNameAtResolutionChoice(Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                                             CardType requiredType,
                                             ChooseSubtypeForSourceEffect followUpSubtypeChoice) implements ChoiceContext {
        public ChooseCardNameAtResolutionChoice(Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                                                CardType requiredType) {
            this(sourceCard, controllerId, sourcePermanentId, requiredType, null);
        }

        public ChooseCardNameAtResolutionChoice(Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
            this(sourceCard, controllerId, sourcePermanentId, null, null);
        }
    }

    record CreateTokenWithChosenNameChoice(Card sourceCard, UUID controllerId, CreateTokenEffect tokenTemplate,
                                           int amount, int power, int toughness) implements ChoiceContext {}

    /**
     * The controller chose a card name; {@code targetPlayerId} reveals their hand, the source deals
     * {@code damagePerCard} damage per revealed copy, then every copy in their hand/graveyard/library
     * is exiled and they shuffle (Thought Hemorrhage). When {@code chooseAnyNumber} is true, the
     * selected card is exiled first and the controller chooses any number of the remaining copies
     * (Pick the Brain). {@code sourceCard} attributes the damage.
     */
    record RevealHandDamageAndExileByNameChoice(UUID targetPlayerId, UUID controllerId,
                                                List<CardType> excludedTypes, int damagePerCard,
                                                Card sourceCard, boolean chooseAnyNumber) implements ChoiceContext {
        public RevealHandDamageAndExileByNameChoice(UUID targetPlayerId, UUID controllerId,
                                                     List<CardType> excludedTypes, int damagePerCard,
                                                     Card sourceCard) {
            this(targetPlayerId, controllerId, excludedTypes, damagePerCard, sourceCard, false);
        }
    }

    /** Assembly Hall: choose the name of a creature card currently in your hand to reveal. */
    record AssemblyHallCreatureCardChoice(UUID controllerId) implements ChoiceContext {}

    /** Infernal Tutor: choose the name of any card currently in your hand to reveal. */
    record InfernalTutorCardChoice(UUID controllerId) implements ChoiceContext {}

    /**
     * First half of Mindblaze: the controller picks a card name. The answer chains into
     * {@link RevealLibraryNumberGuessChoice}, which asks for the number.
     */
    record RevealLibraryNameGuessChoice(UUID targetPlayerId, UUID controllerId,
                                        List<CardType> excludedTypes, int damage,
                                        Card sourceCard) implements ChoiceContext {}

    /**
     * Second half of Mindblaze: the controller picks a number greater than 0 for the already
     * chosen {@code chosenName}. On the answer {@code targetPlayerId} reveals their library, takes
     * {@code damage} damage if it holds exactly that many cards with that name, and shuffles.
     */
    record RevealLibraryNumberGuessChoice(UUID targetPlayerId, UUID controllerId, String chosenName,
                                          int damage, Card sourceCard) implements ChoiceContext {}

    /** The controller names a card, then the targeted opponent guesses whether that name is in the controller's hand. */
    record LiarsPendulumChoice(UUID controllerId, UUID targetPlayerId, UUID sourcePermanentId,
                               Card sourceCard, String chosenName) implements ChoiceContext {}

    /**
     * A single protection choice that applies to every permanent in {@code targetIds} — one pick
     * covering all of a spell's targets ("X target creatures gain protection from the chosen
     * color", Prismatic Boon), which for most cards is a one-element list.
     */
    record ProtectionColorChoice(List<UUID> targetIds, boolean includeArtifacts, boolean includeColorless)
            implements ChoiceContext {

        public ProtectionColorChoice(UUID targetId, boolean includeArtifacts) {
            this(List.of(targetId), includeArtifacts, false);
        }

        public ProtectionColorChoice(List<UUID> targetIds, boolean includeArtifacts) {
            this(targetIds, includeArtifacts, false);
        }
    }

    record PreventDamageToTargetFromChosenColorChoice(UUID targetId) implements ChoiceContext {}

    record TargetCreatureHexproofFromChosenColorChoice(UUID targetId) implements ChoiceContext {}

    record BecomeChosenColorAndGainHexproofChoice(UUID targetId, UUID controllerId,
                                                    String sourceCardName) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution; the target permanent then becomes that color
     * until end of turn (CR 105.3 / layer 5). Used by Distorting Lens.
     *
     * @param targetId       the permanent that becomes the chosen color
     * @param controllerId   controller of the ability that created the effect
     * @param sourceCardName name of the card whose ability created the effect
     */
    record ColorSetChoice(UUID targetId, UUID controllerId, String sourceCardName) implements ChoiceContext {}

    /**
     * The controller chooses one color for all targeted creatures until end of turn.
     */
    record ColorSetTargetsChoice(List<UUID> targetIds, UUID controllerId, String sourceCardName)
            implements ChoiceContext {}

    /**
     * A single color choice that grants the chosen controller and each permanent they control
     * protection from the chosen color until end of turn (e.g. Faith's Shield fateful hour).
     */
    record MassProtectionColorChoice(UUID controllerId) implements ChoiceContext {}

    /** The controller chooses a color and gains protection from it until end of turn. */
    record ControllerProtectionColorChoice(UUID controllerId) implements ChoiceContext {}

    record SubtypeChoice(UUID permanentId, boolean landPlay, boolean continueGameStart,
                         boolean buddyListChoice) implements ChoiceContext {
        public SubtypeChoice(UUID permanentId) {
            this(permanentId, false, false, false);
        }

        public SubtypeChoice(UUID permanentId, boolean landPlay) {
            this(permanentId, landPlay, false, false);
        }

        public SubtypeChoice(UUID permanentId, boolean landPlay, boolean continueGameStart) {
            this(permanentId, landPlay, continueGameStart, false);
        }
    }

    /** The controller chooses one of the newly encountered creature types for their buddy list. */
    record BuddyListChoice(UUID playerId) implements ChoiceContext {}

    record SourceSubtypeChoice(UUID permanentId, boolean untilEndOfTurn) implements ChoiceContext {
        public SourceSubtypeChoice(UUID permanentId) {
            this(permanentId, false);
        }
    }

    /**
     * The controller chooses a creature type at resolution for a spell/ability that has no
     * permanent to store it on (e.g. Coordinated Barrage). The answer is stored on
     * {@code GameData.chosenSpellSubtype} and effect resolution resumes.
     */
    record SpellCreatureTypeChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing a nonbasic land type at resolution for a spell with no permanent to store it on. */
    record SpellNonbasicLandTypeChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing a card type at resolution for a spell with no permanent to store it on. */
    record SpellCardTypeChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing land or nonland at resolution for a spell with no permanent to store it on. */
    record SpellLandOrNonlandChoice(UUID controllerId, boolean secret) implements ChoiceContext {
        public SpellLandOrNonlandChoice(UUID controllerId) {
            this(controllerId, false);
        }
    }

    /** Choosing a color at resolution for a spell with no permanent to store it on. */
    record SpellColorChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing a number at resolution for a spell with no permanent to store it on. */
    /**
     * A number chosen for a spell. When {@code anyNonNegativeNumber} is set the offered options are only
     * suggestions and any non-negative integer is accepted ("choose a number" with no upper bound).
     */
    record SpellNumberChoice(UUID controllerId, boolean anyNonNegativeNumber) implements ChoiceContext {

        public SpellNumberChoice(UUID controllerId) {
            this(controllerId, false);
        }
    }

    /** Choosing odd or even at resolution for a spell with no permanent to store it on. */
    record SpellManaValueParityChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing odd or even "as this permanent enters" (Ashling's Prerogative). */
    record ManaValueParityChoice(UUID permanentId, Card creatureCard, UUID targetId,
                                 boolean wasCastFromHand, int etbMode, int xValue, boolean kicked,
                                 List<UUID> targetIds, List<String> repeatedAdditionalCosts,
                                 List<UUID> convokeCreatureIds) implements ChoiceContext {
        public ManaValueParityChoice {
            targetIds = List.copyOf(targetIds);
            repeatedAdditionalCosts = List.copyOf(repeatedAdditionalCosts);
            convokeCreatureIds = List.copyOf(convokeCreatureIds);
        }

        public ManaValueParityChoice(UUID permanentId) {
            this(permanentId, null, null, false, 0, 0, false, List.of(), List.of(), List.of());
        }
    }

    /**
     * Choosing a number in an inclusive range for {@code permanentId} (e.g. Shapeshifter's "choose a
     * number between 0 and 7", both as it enters and at each upkeep). The answer is stored on the
     * permanent via {@code Permanent.setChosenNumber(int)}.
     */
    /**
     * Krark's Thumb: the controller of {@code permanentId} chooses which result to keep of the coin flipped as it
     * entered (Molten Sentry, CR 614.1c) when the physical flips disagree. On the answer the result fixes the
     * permanent's form and the deferred enter-the-battlefield abilities of {@code card} are processed with the rest of
     * this context. Answered with {@link CoinFlipResultChoice#HEADS} or {@link CoinFlipResultChoice#TAILS}.
     */
    record EntryCoinFlipResultChoice(UUID permanentId, UUID controllerId, Card card, UUID targetId,
                                     boolean wasCastFromHand, int etbMode, int xValue, boolean kicked,
                                     List<UUID> targetIds, List<String> repeatedAdditionalCosts,
                                     List<UUID> convokeCreatureIds) implements ChoiceContext {
        public EntryCoinFlipResultChoice {
            targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
            repeatedAdditionalCosts = repeatedAdditionalCosts == null ? List.of() : List.copyOf(repeatedAdditionalCosts);
            convokeCreatureIds = convokeCreatureIds == null ? List.of() : List.copyOf(convokeCreatureIds);
        }
    }

    record NumberChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * "As this creature enters, pay any amount of life" (Minion of the Wastes). The controller
     * picks 0..the offered maximum as {@code permanentId} enters; on the answer that much life is
     * paid, the amount is stored via {@code Permanent.setChosenNumber(int)}, and the deferred
     * enter-the-battlefield triggers of {@code card} are processed.
     */
    record PayAnyAmountOfLifeAsEnters(UUID permanentId, UUID controllerId, Card card, UUID targetId,
                                      boolean wasCastFromHand, int etbMode,
                                      boolean kicked) implements ChoiceContext {}

    /** The controller chooses counter types for a creature's as-enters replacement. */
    record AsEntersCounterTypeChoice(UUID permanentId, UUID controllerId, Card card, UUID targetId,
                                     boolean wasCastFromHand, int etbMode, int xValue, boolean kicked,
                                     List<UUID> targetIds, int exiledCardCount,
                                     List<CounterType> counterTypes,
                                     boolean requireDifferentCounterTypes) implements ChoiceContext {
        public AsEntersCounterTypeChoice(UUID permanentId, UUID controllerId, Card card, UUID targetId,
                                         boolean wasCastFromHand, int etbMode, int xValue, boolean kicked,
                                         List<UUID> targetIds, int exiledCardCount,
                                         List<CounterType> counterTypes) {
            this(permanentId, controllerId, card, targetId, wasCastFromHand, etbMode, xValue, kicked,
                    targetIds, exiledCardCount, counterTypes, false);
        }

        public AsEntersCounterTypeChoice {
            targetIds = List.copyOf(targetIds);
            counterTypes = List.copyOf(counterTypes);
        }

        public static String label(CounterType counterType) {
            return switch (counterType) {
                case PLUS_TWO_PLUS_ZERO -> "+2/+0";
                case PLUS_ONE_PLUS_ONE -> "+1/+1";
                case PLUS_ZERO_PLUS_TWO -> "+0/+2";
                default -> counterType.name().toLowerCase().replace('_', ' ');
            };
        }
    }

    /**
     * Choosing how many {@code counterType} counters to remove from {@code permanentId} as a
     * storage land's mana ability resolves (0..the count present). On resume the chosen number of
     * counters is removed and that much mana of {@code color} is added to {@code playerId}'s pool
     * (times {@code manaMultiplier} for static mana replacement effects; {@code fromCreature}
     * marks creature mana).
     * Used by the storage-land cycle via {@code RemoveCountersForManaEffect}.
     */
    record RemoveCountersForManaChoice(UUID playerId, UUID permanentId, List<ManaColor> colors,
                                       CounterType counterType, boolean fromCreature,
                                       int manaMultiplier) implements ChoiceContext {

        public RemoveCountersForManaChoice {
            colors = List.copyOf(colors);
        }

        public RemoveCountersForManaChoice(UUID playerId, UUID permanentId, ManaColor color,
                                           CounterType counterType, boolean fromCreature,
                                           int manaMultiplier) {
            this(playerId, permanentId, List.of(color), counterType, fromCreature, manaMultiplier);
        }
    }

    /**
     * Tetravus first upkeep trigger: the controller chooses how many of {@code permanentId}'s +1/+1
     * counters (0..the count present) to remove; on the answer that many are removed and that many
     * Tetravite tokens are created from {@code tokenTemplate}, each recorded as "created with" the
     * source in {@code GameData.sourceCreatedTokens} (read by the paired exile trigger).
     */
    record TetravusCounterRemoval(UUID permanentId,
                                  com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate)
            implements ChoiceContext {}

    /**
     * "Move any number of {@code counterType} counters from target creature onto another target
     * creature" (Bioshift): the spell's controller chooses how many (0..the count on
     * {@code fromPermanentId}); on the answer that many counters are moved onto
     * {@code toPermanentId}.
     */
    record MoveCountersAmountChoice(UUID fromPermanentId, UUID toPermanentId, CounterType counterType,
                                    String sourceCardName) implements ChoiceContext {}

    /** Chooses how many counters of one successive kind to move between two permanents. */
    record MoveAnyNumberOfCountersAmountChoice(UUID fromPermanentId, UUID toPermanentId,
                                               List<CounterType> counterTypes, int index,
                                               String sourceCardName) implements ChoiceContext {
        public MoveAnyNumberOfCountersAmountChoice {
            counterTypes = List.copyOf(counterTypes);
        }
    }

    /**
     * Slippery Bogbonder: the controller chooses how many counters of each concrete kind to move
     * from each controlled creature onto the target creature. {@code movedSoFar} holds the counters
     * already taken from earlier sources; they are put on the target together as one placement once
     * every choice has been made.
     */
    record MoveAnyCountersFromControlledCreaturesAmountChoice(List<CounterSource> sources, int index,
                                                               UUID toPermanentId, String sourceCardName,
                                                               Map<CounterType, Integer> movedSoFar)
            implements ChoiceContext {
        public MoveAnyCountersFromControlledCreaturesAmountChoice {
            sources = List.copyOf(sources);
            movedSoFar = Map.copyOf(movedSoFar);
        }

        public MoveAnyCountersFromControlledCreaturesAmountChoice(List<CounterSource> sources, int index,
                                                                   UUID toPermanentId, String sourceCardName) {
            this(sources, index, toPermanentId, sourceCardName, Map.of());
        }
    }

    record CounterSource(UUID permanentId, CounterType counterType, String cardName) {}

    /**
     * Aetherborn Marauder: the controller chooses how many counters to move from each selected
     * eligible permanent onto the source. The sequence advances to the next permanent after every
     * answer, so zero is a valid choice for each one.
     */
    record MoveCountersFromControlledPermanentsAmountChoice(List<UUID> fromPermanentIds, int index,
                                                             UUID toPermanentId, CounterType counterType,
                                                             String sourceCardName, String fromCardName,
                                                             int countersPerMovedCounter, int countersRemoved)
            implements ChoiceContext {
        public MoveCountersFromControlledPermanentsAmountChoice(List<UUID> fromPermanentIds, int index,
                                                                 UUID toPermanentId, CounterType counterType,
                                                                 String sourceCardName, String fromCardName) {
            this(fromPermanentIds, index, toPermanentId, counterType, sourceCardName, fromCardName, 1, 0);
        }

        public MoveCountersFromControlledPermanentsAmountChoice(List<UUID> fromPermanentIds, int index,
                                                                 UUID toPermanentId, CounterType counterType,
                                                                 String sourceCardName, String fromCardName,
                                                                 int countersPerMovedCounter) {
            this(fromPermanentIds, index, toPermanentId, counterType, sourceCardName, fromCardName,
                    countersPerMovedCounter, 0);
        }

        public MoveCountersFromControlledPermanentsAmountChoice {
            fromPermanentIds = List.copyOf(fromPermanentIds);
            if (countersPerMovedCounter < 1 || countersRemoved < 0) {
                throw new IllegalArgumentException("counter choice values must be non-negative and valid");
            }
        }
    }

    /** Choosing one of Primal Clay's three shapes "as this creature enters". */
    record PrimalClayFormChoice(UUID permanentId) implements ChoiceContext {}

    /** Choosing a base power/toughness form as a permanent enters or is turned face up. */
    record PowerToughnessFormChoice(UUID permanentId, List<PowerToughnessForm> forms,
                                     boolean turnFaceUp) implements ChoiceContext {
        public PowerToughnessFormChoice {
            forms = List.copyOf(forms);
        }
    }

    /**
     * Choosing a basic land type "as ~ enters". When {@code chainSecondAfter} is true, answering
     * the first pick immediately begins a second pick ({@code isSecondChoice=true}) for cards that
     * choose two types (Illusionary Terrain). The second pick stores into
     * {@code Permanent.secondChosenSubtype}. {@code allowedTypes} narrows the offered types for
     * cards that only allow some of them ("choose Island or Swamp" — Roots of Life); an empty list
     * offers all five.
     */
    record BasicLandTypeChoice(UUID permanentId, boolean isSecondChoice, boolean chainSecondAfter,
                               List<CardSubtype> allowedTypes) implements ChoiceContext {
        public BasicLandTypeChoice(UUID permanentId) {
            this(permanentId, false, false, List.of());
        }

        public BasicLandTypeChoice(UUID permanentId, boolean isSecondChoice, boolean chainSecondAfter) {
            this(permanentId, isSecondChoice, chainSecondAfter, List.of());
        }
    }

    /**
     * Choosing a basic land type for a target land: either added "in addition to its other types"
     * (Navigator's Compass) or, when {@code replacing} is {@code true}, replacing the land's other
     * types and mana ability per rule 305.7 (Tideshaper Mystic).
     *
     * @param targetLandId the target land that gains/becomes the chosen basic land type
     * @param duration     how long the granted/overriding type lasts
     * @param replacing    {@code true} to replace the land's types, {@code false} to add
     * @param allowedTypes the offered basic land types; an empty list offers all five
     */
    record AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration, boolean replacing,
                                  List<CardSubtype> allowedTypes) implements ChoiceContext {

        public AddBasicLandTypeChoice {
            allowedTypes = allowedTypes == null ? List.of() : List.copyOf(allowedTypes);
        }

        public AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration, boolean replacing) {
            this(targetLandId, duration, replacing, List.of());
        }

        public AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration) {
            this(targetLandId, duration, false, List.of());
        }
    }

    /**
     * Choosing the land type for a snow landwalk grant: the target creature can't be blocked as
     * long as the defending player controls a land that is both snow and of the chosen type
     * (CR 702.14c), until end of turn. Barbarian Guides.
     *
     * @param targetId the target creature that gains snow landwalk of the chosen type
     */
    record SnowLandwalkGrantChoice(UUID targetId) implements ChoiceContext {
    }

    /**
     * The controller has chosen a basic land type; each land they control becomes that type
     * until end of turn, replacing its other land types/mana ability per rule 305.7
     * (Elsewhere Flask).
     *
     * @param controllerId the player whose lands become the chosen type
     */
    record OwnLandsBecomeBasicTypeChoice(UUID controllerId) implements ChoiceContext {
    }

    /**
     * Vision Charm land mode: choose a land type, then a basic land type; each land of the first
     * type becomes the second until end of turn (rule 305.7). When {@code fromType} is null this
     * is the first pick; after answering, a second pick is begun with {@code fromType} set.
     *
     * @param controllerId the resolving controller making both picks
     * @param fromType     null while choosing the land type; the chosen land type while choosing
     *                     the destination basic land type
     */
    record LandsOfTypeBecomeBasicTypeChoice(UUID controllerId, CardSubtype fromType) implements ChoiceContext {
        public LandsOfTypeBecomeBasicTypeChoice(UUID controllerId) {
            this(controllerId, null);
        }
    }

    /**
     * Tracks the sequential "each player names a card" flow for Conundrum Sphinx etc.
     * Players name in APNAP order. After all have named, top cards are revealed and
     * moved to hand (match) or bottom of library (no match).
     *
     * @param playerOrder  all player IDs in APNAP order
     * @param chosenNames  names chosen so far (playerId → chosen name)
     */
    record EachPlayerCardNameRevealChoice(List<UUID> playerOrder,
                                          Map<UUID, String> chosenNames) implements ChoiceContext {}

    /** Each player chooses a color in APNAP order before a selective permanent exile. */
    record EachPlayerChoosesColorThenExileOtherPermanentsChoice(
            List<UUID> playerOrder, Map<UUID, CardColor> chosenColors) implements ChoiceContext {

        public EachPlayerChoosesColorThenExileOtherPermanentsChoice {
            playerOrder = List.copyOf(playerOrder);
            chosenColors = Map.copyOf(new LinkedHashMap<>(chosenColors));
        }
    }

    /**
     * Sphinx Ambassador: the damaged player names a card after the controller has selected
     * a card from their library. The selected card is stored in
     * the queued {@code PendingSphinxAmbassadorChoice} interaction.
     */
    record SphinxAmbassadorNameChoice(UUID namingPlayerId, UUID controllerId) implements ChoiceContext {}

    /** The damaged player guesses the mana-value range of a card chosen from the controller's hand. */
    record MasterOfPredicamentsGuessChoice(UUID controllerId, Card sourceCard, Card selectedCard,
                                           int guessThreshold, CardEffect incorrectGuessDeclineEffect)
            implements ChoiceContext {
        public MasterOfPredicamentsGuessChoice(UUID controllerId, Card sourceCard, Card selectedCard) {
            this(controllerId, sourceCard, selectedCard, 4, null);
        }
    }

    /**
     * Lammastide Weave: the controller names a card, then the target player mills one card. If the
     * milled card matches the chosen name, the controller gains life equal to its mana value.
     */
    record NameCardMillGainLifeChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * Foreshadow: the controller names a card, then the target player mills one card. If the milled
     * card matches the chosen name, the controller draws a card.
     */
    record NameCardMillDrawChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * Tunnel Vision: the controller names a card, then the target player reveals until finding it.
     * If found, the other revealed cards go to the graveyard and the named card returns to the top;
     * otherwise the target player shuffles their library.
     */
    record ChooseNameRevealUntilNamedPutOnTopRestToGraveyardChoice(UUID controllerId,
                                                                    UUID targetPlayerId)
            implements ChoiceContext {}

    /**
     * The controller names a card, then exiles the top {@code topExileCount} cards of their library
     * and reveals until finding the named card (to hand; rest of the dig exiled). If the named card
     * is never revealed, the entire remaining library is exiled. The controller loses
     * {@code lifeLossPerExiled} life per card exiled by the effect.
     */
    record ChooseNameExileTopRevealUntilNamedChoice(UUID controllerId, int topExileCount,
                                                     int lifeLossPerExiled)
            implements ChoiceContext {}

    /**
     * Wood Sage: the controller names a creature card, then reveals the top {@code count} cards of
     * their library, putting every revealed card with that name into their hand and the rest into
     * their graveyard.
     */
    record ChooseCreatureNameRevealTopCardsChoice(UUID controllerId, Card sourceCard, int count)
            implements ChoiceContext {}

    /**
     * Tamiyo, Collector of Tales: the controller names a nonland card, then reveals the top
     * {@code count} cards of their library, putting matching cards into their hand and the rest
     * into their graveyard.
     */
    record ChooseNonlandCardNameRevealTopCardsChoice(UUID controllerId, Card sourceCard, int count)
            implements ChoiceContext {}

    /**
     * Desperate Research: the controller names a non-basic-land card, then reveals the top
     * {@code count} cards of their library, putting every revealed card with that name into their
     * hand and exiling the rest.
     */
    record ChooseNameRevealTopCardsToHandRestToExileChoice(UUID controllerId, Card sourceCard, int count)
            implements ChoiceContext {}

    /**
     * Comply / Academic Probation: the controller names a card; until their next turn, their
     * opponents can't cast spells with that name. Stamped on
     * {@code GameData.opponentsCantCastNamedSpellsUntilControllerNextTurn}.
     */
    record OpponentsCantCastNamedSpellsUntilNextTurnChoice(UUID controllerId,
                                                           boolean restrictToAllowedNames,
                                                           List<CardType> excludedTypes)
            implements ChoiceContext {

        public OpponentsCantCastNamedSpellsUntilNextTurnChoice {
            excludedTypes = List.copyOf(excludedTypes);
        }

        public OpponentsCantCastNamedSpellsUntilNextTurnChoice(UUID controllerId,
                                                              boolean restrictToAllowedNames) {
            this(controllerId, restrictToAllowedNames, List.of());
        }

        public OpponentsCantCastNamedSpellsUntilNextTurnChoice(UUID controllerId) {
            this(controllerId, false);
        }
    }

    /**
     * Conjurer's Ban: the controller names a card; until their next turn, spells with that name
     * can't be cast and lands with that name can't be played.
     */
    record SpellsAndLandsCantBePlayedUntilNextTurnChoice(UUID controllerId) implements ChoiceContext {}

    /**
     * The target player names a card, then reveals the top card of their library. If it matches
     * the named card it goes to their hand; otherwise it goes to their graveyard and the source
     * ({@code sourcePermanentId}) deals {@code damageOnMiss} damage to them ({@code 0} for no
     * damage). Used by Vexing Arcanix.
     */
    record TargetPlayerNameCardRevealTopChoice(UUID controllerId, UUID targetPlayerId, UUID sourcePermanentId,
                                               int damageOnMiss, Card sourceCard) implements ChoiceContext {
        public TargetPlayerNameCardRevealTopChoice(UUID controllerId, UUID targetPlayerId,
                                                   UUID sourcePermanentId, int damageOnMiss) {
            this(controllerId, targetPlayerId, sourcePermanentId, damageOnMiss, null);
        }
    }

    /**
     * Diviner's Lockbox: the controller names a card, then reveals the top card of their library.
     * A matching card inserts the source sacrifice and three-card draw into the paused ability.
     */
    record ChooseCardNameRevealTopCardChoice(UUID controllerId) implements ChoiceContext {}

    record ChooseCardNameForDelayedCreatureCombatDamageChoice(
            UUID controllerId,
            List<CardEffect> effects,
            Card sourceCard,
            boolean combatDamageToPlayerOnly,
            boolean untilEndOfTurn
    ) implements ChoiceContext {

        public ChooseCardNameForDelayedCreatureCombatDamageChoice {
            effects = List.copyOf(effects);
        }
    }

    /**
     * Cursed Scroll: the controller names a card, then reveals a card at random from their own hand.
     * If the revealed card has that name, {@code sourceCard} deals {@code damage} damage to
     * {@code targetId} (the any-target chosen when the ability was activated).
     */
    record ChooseNameRevealRandomHandCardDamageChoice(UUID controllerId, UUID targetId, UUID sourcePermanentId,
                                                     Card sourceCard, int damage) implements ChoiceContext {}

    /**
     * Nebuchadnezzar: the controller names a card, then the target player reveals cards at random
     * from their hand and discards the revealed cards with the chosen name.
     */
    record ChooseNameRevealRandomHandCardsDiscardChoice(UUID controllerId, UUID targetPlayerId,
                                                        Card sourceCard, int revealCount)
            implements ChoiceContext {}

    /** The controller names a card; the target reveals their hand and discards matching cards. */
    record ChooseNameRevealHandDiscardChoice(UUID controllerId, UUID targetPlayerId,
                                              boolean discardOnlyOneAndDrawIfNoMatch)
            implements ChoiceContext {

        public ChooseNameRevealHandDiscardChoice(UUID controllerId, UUID targetPlayerId) {
            this(controllerId, targetPlayerId, false);
        }
    }

    /** The controller names a card; the damaged player reveals their hand and a matching name
     * inserts the follow-up effect into the paused resolution. */
    record ChooseCardNameRevealHandThenChoice(UUID controllerId, UUID targetPlayerId,
                                               CardEffect followUpEffect) implements ChoiceContext {}

    /**
     * The controller chooses a permanent type at resolution time (e.g. Creeping Renaissance),
     * then all cards of that type are returned from the controller's graveyard.
     */
    record PermanentTypeChoice(UUID controllerId, GraveyardChoiceDestination destination,
                               String entryDescription) implements ChoiceContext {}

    /**
     * Distributes {@code remaining} mana among a fixed set of colors, one mana per answer, for "add N
     * mana in any combination of {R} and/or {G}" (Grand Warlord Radha). Each answer adds one mana of
     * the chosen color that doesn't empty from the pool until end of turn, then re-prompts until all
     * of it has been assigned.
     *
     * @param playerId  the player who chooses and receives the mana
     * @param remaining how much mana is still to be assigned a color
     * @param colors    the colors each mana may be
     */
    record PersistentManaDistributionChoice(UUID playerId, int remaining, List<ManaColor> colors)
            implements ChoiceContext {
        public PersistentManaDistributionChoice {
            colors = List.copyOf(colors);
        }

        /** The color prompt that assigns the next mana of this distribution. */
        public PendingInteraction.ColorChoice prompt() {
            return new PendingInteraction.ColorChoice(playerId, null, null, this,
                    colors.stream().map(Enum::name).toList(),
                    "Choose a color for the next mana (" + remaining + " left to add).");
        }
    }

    /**
     * The controller chooses a color at resolution, then {@code targetPlayerId} reveals their hand
     * and discards every card of that color (Persecute).
     */
    record DiscardChosenColorChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then chooses one card of that color from the
     * target player's revealed hand for that player to discard.
     */
    record ChooseColorThenDiscardFromTargetHandChoice(UUID controllerId, UUID targetPlayerId)
            implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then every permanent of that color returns to
     * its owner's hand (Wash Out).
     */
    record ReturnAllPermanentsOfChosenColorChoice(UUID controllerId, PermanentPredicate filter)
            implements ChoiceContext {

        public ReturnAllPermanentsOfChosenColorChoice(UUID controllerId) {
            this(controllerId, null);
        }
    }

    /** The controller chooses a color at resolution, then destroys every matching permanent. */
    record DestroyAllPermanentsOfChosenColorChoice(UUID controllerId, PermanentPredicate filter)
            implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution; {@code targetPlayerId} then exiles the top
     * {@code count} cards of their library and, for each exiled card of the chosen color, the
     * controller creates a token from {@code tokenTemplate} (Oona, Queen of the Fae).
     *
     * @param controllerId   controller that chooses the color and creates the tokens
     * @param targetPlayerId the target opponent who exiles cards
     * @param count          number of top cards to exile
     * @param tokenTemplate  one token created per exiled card of the chosen color
     * @param sourceSetCode  set code of the source card (token art/set)
     */
    record ExileTopCardsChosenColorTokensChoice(UUID controllerId, UUID targetPlayerId, int count,
                                                com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                                String sourceSetCode) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then creates one token from {@code tokenTemplate}
     * for each permanent of that color on the battlefield (any controller; lands excluded, mirroring
     * Oona). Rith, the Awakener.
     *
     * @param controllerId  controller that chooses the color and creates the tokens
     * @param tokenTemplate one token created per permanent of the chosen color
     * @param sourceSetCode set code of the source card (token art/set)
     */
    record CreateTokensPerPermanentOfChosenColorChoice(UUID controllerId,
                                                       com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                                       String sourceSetCode) implements ChoiceContext {}

    /**
     * Jinnie Fay's Cat, Dog or original tokens choice as her replacement applies to {@code event}, a token creation
     * event (blueprint or token-copy) whose other replacements are ordered with hers (CR 616.1); the event continues
     * with the chosen tokens through the replacement order once the player answers.
     */
    record JinnieFayTokenChoice(UUID controllerId, Card sourceCard,
                                com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent event)
            implements ChoiceContext {}

    /** The controller chooses a color at resolution, then gains one life per matching permanent. */
    record GainLifePerPermanentOfChosenColorChoice(UUID controllerId, Card sourceCard,
                                                   StackEntryType sourceEntryType) implements ChoiceContext {}

    /**
     * Hall of Gemstone: {@code playerId} (the player whose upkeep it is) chooses a color; until end
     * of turn every land tapped for mana produces that color instead of any other color.
     */
    record AllLandsProduceChosenColorChoice(UUID playerId) implements ChoiceContext {}

    /** The controller is choosing the two distinct colors stored by Tablet of the Guilds. */
    record ChooseTwoColorsOnEnterChoice(UUID permanentId, UUID etbTargetId,
                                        List<CardColor> chosen) implements ChoiceContext {}

    /** The controller is choosing up to two colors represented by cards in hand. */
    record ChooseUpToTwoColorsOnEnterChoice(UUID permanentId, UUID etbTargetId,
                                            List<CardColor> allowedColors,
                                            List<CardColor> chosen) implements ChoiceContext {
        public ChooseUpToTwoColorsOnEnterChoice {
            allowedColors = List.copyOf(allowedColors);
            chosen = List.copyOf(chosen);
        }
    }

    /**
     * Storage Matrix: during {@code playerId}'s untap step the active player chooses artifact,
     * creature, or land; only permanents of the chosen type untap this step.
     */
    record StorageMatrixUntapChoice(UUID playerId) implements ChoiceContext {}

    /** Turnabout: choose whether to tap or untap artifacts, creatures, or lands. */
    record TurnaboutChoice(UUID playerId) implements ChoiceContext {}

    /**
     * Teferi's Realm: {@code playerId} (the player whose upkeep it is) chooses artifact, creature,
     * land, or non-Aura enchantment; all nontoken permanents of that type then phase out.
     *
     * @param playerId   the choosing (active) player
     * @param sourceCard the Teferi's Realm permanent's card (for logging / phase-out source)
     */
    record TeferisRealmTypeChoice(UUID playerId, Card sourceCard) implements ChoiceContext {}

    /**
     * Controller chooses one or more colors for {@code targetId}, which then becomes those colors
     * for {@code duration} (until end of turn for Prismwake Merrow / Scuttlemutt; indefinitely for
     * Shyft). Colors are picked one at a time (with a "DONE" option once at least one is chosen);
     * {@code chosen} accumulates the picks so far.
     *
     * @param targetId       the permanent that becomes the chosen colors
     * @param sourceCardName name of the card whose ability created the effect (for display)
     * @param chosen         the colors picked so far
     * @param duration       how long the color set lasts
     */
    record BecomeChosenColorsChoice(UUID targetId, String sourceCardName,
                                    List<CardColor> chosen,
                                    EffectDuration duration) implements ChoiceContext {}

    /**
     * Relic Bind's "choose one" mode pick, made as the enchanted-artifact-tap triggered ability
     * resolves. {@code sourceCard} is the Aura (used for the follow-up target choice and logging);
     * {@code controllerId} chooses the mode and then the target. The two options are
     * {@link #DAMAGE} (deal 1 damage to target player or planeswalker) and {@link #LIFE}
     * (target player gains 1 life).
     */
    record RelicBindModeChoice(Card sourceCard, UUID controllerId) implements ChoiceContext {

        public static final String DAMAGE = "Deal 1 damage to target player or planeswalker";
        public static final String LIFE = "Target player gains 1 life";
        public static final List<String> OPTIONS = List.of(DAMAGE, LIFE);
    }

    /**
     * Hullbreaker Horror's "choose up to one" mode pick, made as the spell-cast triggered ability
     * resolves. {@code sourceCard} is Hullbreaker Horror; {@code controllerId} chooses the mode and
     * then (for SPELL / PERMANENT) the target. {@link #NONE} skips both modes.
     */
    record HullbreakerHorrorModeChoice(Card sourceCard, UUID controllerId) implements ChoiceContext {

        public static final String SPELL = "Return target spell you don't control to its owner's hand";
        public static final String PERMANENT = "Return target nonland permanent to its owner's hand";
        public static final String NONE = "Do nothing";
    }

    /**
     * Quarry Hauler: "for each kind of counter on target permanent, put another counter of that kind
     * on it or remove one from it." The controller answers {@link #ADD}/{@link #REMOVE} once for the
     * first entry of {@code remainingKinds}; the answer is applied to {@code targetId} and, if any
     * kinds are left, the choice re-prompts for the next one until every kind has been resolved.
     *
     * @param targetId       the permanent whose counters are being adjusted
     * @param controllerId   the player making the add/remove decisions
     * @param sourceCardName name of the source card (for the prompt/log)
     * @param remainingKinds the counter kinds still awaiting a decision (first is the current one)
     */
    record AdjustCounterKindChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                   List<CounterType> remainingKinds) implements ChoiceContext {

        public static final String ADD = "ADD";
        public static final String REMOVE = "REMOVE";
        public static final List<String> OPTIONS = List.of(ADD, REMOVE);
    }

    /** Price of Betrayal's per-counter-kind removal choice. */
    record RemoveCountersOfKindChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                      List<String> counterKinds, int index, int remaining,
                                      UUID boostSourcePermanentId, int removedSoFar)
            implements ChoiceContext {

        public RemoveCountersOfKindChoice {
            counterKinds = List.copyOf(counterKinds);
        }

        /**
         * A plain removal choice. A non-null {@code boostSourcePermanentId} makes the permanent get
         * +1/+0 until end of turn for each counter removed over the whole choice (Hex Parasite).
         */
        public RemoveCountersOfKindChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                          List<String> counterKinds, int index, int remaining) {
            this(targetId, controllerId, sourceCardName, counterKinds, index, remaining, null, 0);
        }

        public static String counterLabel(String counterKind) {
            return switch (counterKind) {
                case "POISON" -> "poison";
                case "ENERGY" -> "energy";
                case "PLUS_ONE_PLUS_ONE" -> "+1/+1";
                case "MINUS_ONE_MINUS_ONE" -> "-1/-1";
                default -> counterKind.toLowerCase().replace('_', ' ');
            };
        }
    }

    /** Clockspinning's choice of a counter on a permanent or suspended card. */
    record AdjustChosenCounterTypeChoice(UUID targetId, Zone targetZone, UUID controllerId,
                                         String sourceCardName, List<CounterType> counterTypes)
            implements ChoiceContext {

        public AdjustChosenCounterTypeChoice {
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            return counterTypes.stream().map(AdjustChosenCounterTypeChoice::counterLabel).toList();
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    /** Clockspinning's choice to add or remove the selected counter. */
    record AdjustChosenCounterActionChoice(UUID targetId, Zone targetZone, UUID controllerId,
                                           String sourceCardName, CounterType counterType)
            implements ChoiceContext {

        public static final String ADD = "ADD";
        public static final String REMOVE = "REMOVE";
        public static final String SKIP = "SKIP";
        public static final List<String> OPTIONS = List.of(ADD, REMOVE, SKIP);
    }

    /** Choice of a counter kind to add to the target. */
    record AddAnotherCounterTypeChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                       List<CounterType> counterTypes, boolean poisonCounters,
                                       boolean distributeToOtherControlledCreatures,
                                       UUID placementTargetId, boolean energyCounters)
            implements ChoiceContext {

        public AddAnotherCounterTypeChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                           List<CounterType> counterTypes, boolean poisonCounters,
                                           boolean distributeToOtherControlledCreatures, UUID placementTargetId) {
            this(targetId, controllerId, sourceCardName, counterTypes, poisonCounters,
                    distributeToOtherControlledCreatures, placementTargetId, false);
        }

        public AddAnotherCounterTypeChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                           List<CounterType> counterTypes, boolean poisonCounters) {
            this(targetId, controllerId, sourceCardName, counterTypes, poisonCounters, false, null);
        }

        public AddAnotherCounterTypeChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                           List<CounterType> counterTypes, boolean poisonCounters,
                                           boolean distributeToOtherControlledCreatures) {
            this(targetId, controllerId, sourceCardName, counterTypes, poisonCounters,
                    distributeToOtherControlledCreatures, null);
        }

        public static final String POISON = "poison counters";
        public static final String ENERGY = "energy counters";

        public List<String> options() {
            List<String> options = new ArrayList<>(counterTypes.stream()
                    .map(AddAnotherCounterTypeChoice::counterLabel).toList());
            if (poisonCounters) options.add(POISON);
            if (energyCounters) options.add(ENERGY);
            return options;
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    /** Bribe Taker's optional counter choice for the next controlled counter kind. */
    record ChooseCounterForEachControlledCounterKindChoice(
            UUID sourcePermanentId, UUID controllerId, String sourceCardName,
            List<CounterType> remainingKinds) implements ChoiceContext {

        public static final String SKIP = "Don't put a counter";

        public ChooseCounterForEachControlledCounterKindChoice {
            remainingKinds = List.copyOf(remainingKinds);
        }

        public CounterType currentKind() {
            return remainingKinds.getFirst();
        }

        public List<String> options() {
            List<String> options = new ArrayList<>();
            options.add(SKIP);
            options.add(counterLabel(CounterType.PLUS_ONE_PLUS_ONE));
            if (currentKind() != CounterType.PLUS_ONE_PLUS_ONE) {
                options.add(counterLabel(currentKind()));
            }
            return options;
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    /** Invoke the Ancients: choose a keyword counter for the next created Spirit token. */
    record CreateTokenCounterChoice(Card sourceCard, UUID controllerId, UUID tokenId,
                                   List<UUID> remainingTokenIds, List<CounterType> counterTypes)
            implements ChoiceContext {

        public CreateTokenCounterChoice {
            remainingTokenIds = List.copyOf(remainingTokenIds);
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            return counterTypes.stream().map(CreateTokenCounterChoice::counterLabel).toList();
        }

        public static String counterLabel(CounterType counterType) {
            return counterType.name().toLowerCase().replace('_', ' ');
        }
    }

    record RemoveChosenCountersChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                      int remainingSelections, List<CounterType> counterTypes,
                                      boolean exactAmount)
            implements ChoiceContext {

        public static final String DONE = "Done";

        public RemoveChosenCountersChoice {
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            List<String> options = new java.util.ArrayList<>(counterTypes.stream()
                    .map(RemoveChosenCountersChoice::counterLabel)
                    .toList());
            options.add(DONE);
            return List.copyOf(options);
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    record RemoveUpToCountersFromAllPermanentsChoice(StackEntry resolvingEntry, CounterType counterType,
                                                     int remaining, Map<String, UUID> permanentOptions)
            implements ChoiceContext {

        public static final String DONE = "Done";

        public RemoveUpToCountersFromAllPermanentsChoice {
            permanentOptions = java.util.Collections.unmodifiableMap(
                    new java.util.LinkedHashMap<>(permanentOptions));
        }

        public List<String> options() {
            List<String> options = new java.util.ArrayList<>(permanentOptions.keySet());
            options.add(DONE);
            return List.copyOf(options);
        }
    }

    /** Chooses controlled permanents from which an optional forced counter cost removes counters. */
    record RemoveCountersFromForcedCostOrElse(PendingMayAbility ability,
                                              com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect effect,
                                              UUID payerId, int remaining,
                                              Map<String, CounterSelection> permanentOptions)
            implements ChoiceContext {

        public RemoveCountersFromForcedCostOrElse {
            permanentOptions = java.util.Collections.unmodifiableMap(
                    new java.util.LinkedHashMap<>(permanentOptions));
        }

        public List<String> options() {
            return List.copyOf(permanentOptions.keySet());
        }
    }

    record CounterSelection(UUID permanentId, CounterType counterType) {
    }

    record RemoveAnyNumberOfCountersFromAllPermanentsChoice(
            StackEntry resolvingEntry, Map<String, CounterSelection> counterOptions)
            implements ChoiceContext {

        public static final String DONE = "Done";

        public RemoveAnyNumberOfCountersFromAllPermanentsChoice {
            counterOptions = java.util.Collections.unmodifiableMap(
                    new java.util.LinkedHashMap<>(counterOptions));
        }

        public List<String> options() {
            List<String> options = new java.util.ArrayList<>(counterOptions.keySet());
            options.add(DONE);
            return List.copyOf(options);
        }
    }

    record RemoveOneCounterChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                  List<CounterType> counterTypes, boolean requireControl)
            implements ChoiceContext {

        public RemoveOneCounterChoice {
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            return counterTypes.stream()
                    .map(RemoveChosenCountersChoice::counterLabel)
                    .toList();
        }
    }

    /** Agent's Toolkit's choice of which counter to move onto an entering creature. */
    record MoveOneCounterChoice(UUID sourcePermanentId, UUID targetId, UUID controllerId,
                                String sourceCardName, List<CounterType> counterTypes)
            implements ChoiceContext {

        public MoveOneCounterChoice {
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            return counterTypes.stream()
                    .map(RemoveChosenCountersChoice::counterLabel)
                    .toList();
        }
    }

    /**
     * The controller picks which kind of counter a "remove a counter from this permanent" activation
     * cost spends when the source holds several kinds. Carries the suspended activation so it can be
     * re-entered with the chosen kind. When the cost removes several counters ({@code required} above
     * one) the kinds are picked one counter at a time and {@code chosenSoFar} holds the earlier picks.
     */
    record SourceCounterCostTypeChoice(UUID sourcePermanentId, Integer abilityIndex, Integer xValue,
                                       UUID targetId, Zone targetZone, List<UUID> targetIds,
                                       Map<UUID, Integer> damageAssignments,
                                       List<CounterType> counterTypes, int required,
                                       List<CounterType> chosenSoFar) implements ChoiceContext {

        public SourceCounterCostTypeChoice {
            targetIds = targetIds != null ? List.copyOf(targetIds) : null;
            damageAssignments = damageAssignments != null ? Map.copyOf(damageAssignments) : null;
            counterTypes = List.copyOf(counterTypes);
            chosenSoFar = List.copyOf(chosenSoFar);
        }

        public List<String> options() {
            return counterTypes.stream().map(AdjustChosenCounterTypeChoice::counterLabel).toList();
        }
    }

    /**
     * The controller picks which kind of counter to remove from the permanent they chose to pay a
     * "remove a counter from a creature you control" activation cost with. Carries the suspended
     * permanent-choice context so payment can resume with the chosen kind.
     */
    record ControlledCounterCostTypeChoice(PermanentChoiceContext.ActivatedAbilityCostChoice costChoice,
                                           UUID chosenPermanentId,
                                           List<CounterType> counterTypes) implements ChoiceContext {

        public ControlledCounterCostTypeChoice {
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            return counterTypes.stream().map(AdjustChosenCounterTypeChoice::counterLabel).toList();
        }
    }

    /** Dismantle's choice of counter type for the destroyed artifact's counters. */
    record DismantleCounterTypeChoice(int counterCount, String sourceCardName) implements ChoiceContext {

        public static final String PLUS_ONE_PLUS_ONE = "+1/+1 counters";
        public static final String CHARGE = "charge counters";
        public static final List<String> OPTIONS = List.of(PLUS_ONE_PLUS_ONE, CHARGE);
    }

    /**
     * Mode announcement for a modal triggered ability that is already on the stack but whose mode
     * has not been chosen yet (CR 603.3c). {@code stackEntryId} is the entry's
     * {@link StackEntry#getTargetableId()}; {@code effect} holds only the modes that can legally be
     * chosen.
     */
    record StackedModalTriggerModeChoice(UUID stackEntryId, UUID chooserId, Card sourceCard,
                                         ChooseOneEffect effect) implements ChoiceContext {}

    /**
     * A modal triggered ability's "choose one" mode pick, made as the ability resolves (the engine
     * has no cast-time modal machinery for triggered abilities). {@code sourceCard} is the ability's
     * source (used for logging), {@code controllerId} chooses the mode, and {@code effect} carries the
     * {@link ChooseOneEffect}'s options; the chosen mode's effects are spliced into the paused
     * resolution. Used by non-targeting modal upkeep triggers such as Etherwrought Page.
     *
     * <p>A "choose one that hasn't been chosen" trigger (Demonic Pact) instead picks its mode as the
     * ability goes on the stack: {@code triggerTime} is true, {@code sourcePermanentId} identifies the
     * permanent whose consumed modes are recorded, and the chosen mode's effects become their own
     * triggered ability (with that mode's targets) rather than being spliced into a resolution.
     * A targeted modal trigger also sets {@code triggerTime} without setting {@code consumeMode}.
     * {@code asEnters} records a named mode on a permanent before its ETB abilities are collected.
     */
    record ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                            boolean triggerTime, UUID sourcePermanentId, boolean consumeMode,
                            List<String> chosenLabels, boolean asEnters) implements ChoiceContext {

        public ChooseModeChoice {
            chosenLabels = chosenLabels == null ? List.of() : List.copyOf(chosenLabels);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect) {
            this(sourceCard, controllerId, effect, false, null, false, List.of(), false);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                boolean triggerTime, UUID sourcePermanentId, boolean consumeMode) {
            this(sourceCard, controllerId, effect, triggerTime, sourcePermanentId, consumeMode, List.of(), false);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                boolean triggerTime, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effect, triggerTime, sourcePermanentId, false, List.of(), false);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                UUID sourcePermanentId, boolean asEnters) {
            this(sourceCard, controllerId, effect, false, sourcePermanentId, false, List.of(), asEnters);
        }
    }

    /** Plea for Power: the current player voted for time or knowledge. */
    record PleaForPowerChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                              Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String TIME = "Time";
        public static final String KNOWLEDGE = "Knowledge";
        public static final List<String> OPTIONS = List.of(TIME, KNOWLEDGE);

        public PleaForPowerChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public PleaForPowerChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new PleaForPowerChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /** Travel Through Caradhras: the current player voted for Redhorn Pass or Mines of Moria. */
    record TravelThroughCaradhrasChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                        int redhornPassVotes, int minesOfMoriaVotes,
                                        String sourceName) implements ChoiceContext, VoteChoiceContext {
        public static final String REDHORN_PASS = "Redhorn Pass";
        public static final String MINES_OF_MORIA = "Mines of Moria";
        public static final List<String> OPTIONS = List.of(REDHORN_PASS, MINES_OF_MORIA);

        public TravelThroughCaradhrasChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public TravelThroughCaradhrasChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new TravelThroughCaradhrasChoice(effectControllerId, remainingVoterIds,
                    redhornPassVotes, minesOfMoriaVotes, sourceName);
        }
    }

    /** Sail into the West: the current player voted for return or embark. */
    record SailIntoTheWestChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                 int returnVotes, int embarkVotes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String RETURN = "Return";
        public static final String EMBARK = "Embark";
        public static final List<String> OPTIONS = List.of(RETURN, EMBARK);

        public SailIntoTheWestChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public SailIntoTheWestChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new SailIntoTheWestChoice(effectControllerId, remainingVoterIds,
                    returnVotes, embarkVotes, sourceName);
        }
    }

    /** Master of Ceremonies: the current opponent chooses money, friends, or secrets. */
    record MasterOfCeremoniesChoice(UUID effectControllerId, List<UUID> remainingOpponentIds,
                                    List<UUID> moneyPlayerIds, List<UUID> friendsPlayerIds,
                                    List<UUID> secretsPlayerIds, String sourceName) implements ChoiceContext {
        public static final String MONEY = "Money";
        public static final String FRIENDS = "Friends";
        public static final String SECRETS = "Secrets";
        public static final List<String> OPTIONS = List.of(MONEY, FRIENDS, SECRETS);

        public MasterOfCeremoniesChoice {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
            moneyPlayerIds = List.copyOf(moneyPlayerIds);
            friendsPlayerIds = List.copyOf(friendsPlayerIds);
            secretsPlayerIds = List.copyOf(secretsPlayerIds);
        }
    }

    /** Prisoner's Dilemma: the current opponent secretly chooses silence or snitch. */
    record PrisonersDilemmaChoice(UUID currentPlayerId, List<UUID> remainingOpponentIds,
                                  Map<UUID, String> choices, String sourceName) implements ChoiceContext {
        public static final String SILENCE = "Silence";
        public static final String SNITCH = "Snitch";
        public static final List<String> OPTIONS = List.of(SILENCE, SNITCH);

        public PrisonersDilemmaChoice {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
            choices = Map.copyOf(new java.util.LinkedHashMap<>(choices));
        }
    }

    /** Expropriate: the current player voted for time or money. */
    record ExpropriateChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                             List<UUID> moneyVoterIds, int timeVotes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String TIME = "Time";
        public static final String MONEY = "Money";
        public static final List<String> OPTIONS = List.of(TIME, MONEY);

        public ExpropriateChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            moneyVoterIds = List.copyOf(moneyVoterIds);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public ExpropriateChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new ExpropriateChoice(effectControllerId, remainingVoterIds,
                    moneyVoterIds, timeVotes, sourceName);
        }
    }

    /** Tivit: the current player voted for evidence or bribery. */
    record CouncilDilemmaChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                int evidenceVotes, int briberyVotes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String EVIDENCE = "Evidence";
        public static final String BRIBERY = "Bribery";
        public static final List<String> OPTIONS = List.of(EVIDENCE, BRIBERY);

        public CouncilDilemmaChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public CouncilDilemmaChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new CouncilDilemmaChoice(effectControllerId, remainingVoterIds,
                    evidenceVotes, briberyVotes, sourceName);
        }
    }

    /**
     * "While voting, you may vote an additional time": the voter who just answered {@code answeredVote}
     * with {@code answer} (a word vote) or {@code selectedIds} (a vote for objects or players) chooses
     * whether to vote again before that vote is counted. A {@code null} {@code answeredVote} belongs to a
     * vote that keeps its own progress and reruns its effect for the answer (Truth or Consequences).
     */
    record AdditionalVoteChoice(UUID voterId, PendingInteraction answeredVote, String answer,
                                List<UUID> selectedIds, String sourceName) implements ChoiceContext {
        public static final String VOTE_AGAIN = "Vote again";
        public static final String CONTINUE = "Continue voting";
        public static final List<String> OPTIONS = List.of(VOTE_AGAIN, CONTINUE);

        public AdditionalVoteChoice {
            selectedIds = selectedIds == null ? List.of() : List.copyOf(selectedIds);
        }
    }

    /** Zndrsplt's Judgment: the current player chooses friend or foe. */
    record ZndrsplatsJudgmentChoice(UUID choosingPlayerId, List<UUID> remainingPlayerIds,
                                    List<UUID> chosenPlayerIds, List<UUID> friendPlayerIds,
                                    List<UUID> foePlayerIds,
                                    String sourceName) implements ChoiceContext {
        public static final String FRIEND = "Friend";
        public static final String FOE = "Foe";
        public static final List<String> OPTIONS = List.of(FRIEND, FOE);

        public ZndrsplatsJudgmentChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenPlayerIds = List.copyOf(chosenPlayerIds);
            friendPlayerIds = List.copyOf(friendPlayerIds);
            foePlayerIds = List.copyOf(foePlayerIds);
        }
    }

    /** Seize the Spotlight: the current opponent chooses fame or fortune. */
    record SeizeTheSpotlightChoice(UUID effectControllerId, UUID choosingPlayerId,
                                   List<UUID> remainingPlayerIds, List<UUID> famePlayerIds,
                                   List<UUID> fortunePlayerIds, String sourceName) implements ChoiceContext {
        public static final String FAME = "Fame";
        public static final String FORTUNE = "Fortune";
        public static final List<String> OPTIONS = List.of(FAME, FORTUNE);

        public SeizeTheSpotlightChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            famePlayerIds = List.copyOf(famePlayerIds);
            fortunePlayerIds = List.copyOf(fortunePlayerIds);
        }
    }

    /** Fateful Tempest: the current player voted for past or present. */
    record FatefulTempestChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String PAST = "Past";
        public static final String PRESENT = "Present";
        public static final List<String> OPTIONS = List.of(PAST, PRESENT);

        public FatefulTempestChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public FatefulTempestChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new FatefulTempestChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /** Selvala's Stampede: the current player voted for wild or free. */
    record SelvalasStampedeChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                  Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String WILD = "Wild";
        public static final String FREE = "Free";
        public static final List<String> OPTIONS = List.of(WILD, FREE);

        public SelvalasStampedeChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public SelvalasStampedeChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new SelvalasStampedeChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /** Split Decision: the current player voted for denial or duplication. */
    record VoteForDenialOrDuplicationChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                             Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String DENIAL = "Denial";
        public static final String DUPLICATION = "Duplication";
        public static final List<String> OPTIONS = List.of(DENIAL, DUPLICATION);

        public VoteForDenialOrDuplicationChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public VoteForDenialOrDuplicationChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new VoteForDenialOrDuplicationChoice(effectControllerId, remainingVoterIds,
                    votes, sourceName);
        }
    }

    /** Tyrant's Choice: the current player voted for death or torture. */
    record TyrantsChoiceChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                               Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String DEATH = "Death";
        public static final String TORTURE = "Torture";
        public static final List<String> OPTIONS = List.of(DEATH, TORTURE);

        public TyrantsChoiceChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public TyrantsChoiceChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new TyrantsChoiceChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /** Magister of Worth: the current player voted for grace or condemnation. */
    record GraceOrCondemnationChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                     Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String GRACE = "Grace";
        public static final String CONDEMNATION = "Condemnation";
        public static final List<String> OPTIONS = List.of(GRACE, CONDEMNATION);

        public GraceOrCondemnationChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public GraceOrCondemnationChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new GraceOrCondemnationChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /** Coercive Portal: the current player voted for carnage or homage. */
    record CoercivePortalChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String CARNAGE = "Carnage";
        public static final String HOMAGE = "Homage";
        public static final List<String> OPTIONS = List.of(CARNAGE, HOMAGE);

        public CoercivePortalChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public CoercivePortalChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new CoercivePortalChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /** Path of the Ghosthunter: the current player voted for planeswalk or chaos. */
    record WillOfThePlaneswalkersChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                        Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String PLANESWALK = "Planeswalk";
        public static final String CHAOS = "Chaos";
        public static final List<String> OPTIONS = List.of(PLANESWALK, CHAOS);

        public WillOfThePlaneswalkersChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public WillOfThePlaneswalkersChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new WillOfThePlaneswalkersChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /** Galadriel, Elven-Queen: the current player voted for dominion or guidance. */
    record GaladrielElvenQueenChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                     Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String DOMINION = "Dominion";
        public static final String GUIDANCE = "Guidance";
        public static final List<String> OPTIONS = List.of(DOMINION, GUIDANCE);

        public GaladrielElvenQueenChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public GaladrielElvenQueenChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new GaladrielElvenQueenChoice(effectControllerId, remainingVoterIds, votes, sourceName);
        }
    }

    /**
     * Each player chooses one token option from a resolving effect. The choices made so far are kept in
     * APNAP order and their tokens are created only after the last player has chosen.
     */
    record EachPlayerChoosesTokenChoice(
            com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesTokenEffect effect,
            List<UUID> remainingPlayerIds,
            Map<UUID, com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesTokenEffect.TokenOption> chosenOptions,
            String sourceName) implements ChoiceContext {

        public EachPlayerChoosesTokenChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenOptions = Collections.unmodifiableMap(new LinkedHashMap<>(chosenOptions));
        }
    }

    /** Each player chooses a mode for a face-up planar object. */
    record EachPlayerChoosesPlanarModeChoice(
            com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPlanarModeEffect effect,
            UUID planarObjectId, List<UUID> remainingPlayerIds, String sourceName) implements ChoiceContext {

        public EachPlayerChoosesPlanarModeChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
        }
    }

    /** Elrond of the White Council: the current player voted for fellowship or aid. */
    record ElrondOfTheWhiteCouncilChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                         List<UUID> fellowshipVoterIds, int aidVotes,
                                         String sourceName) implements ChoiceContext, VoteChoiceContext {
        public static final String FELLOWSHIP = "Fellowship";
        public static final String AID = "Aid";
        public static final List<String> OPTIONS = List.of(FELLOWSHIP, AID);

        public ElrondOfTheWhiteCouncilChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            fellowshipVoterIds = List.copyOf(fellowshipVoterIds);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public ElrondOfTheWhiteCouncilChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new ElrondOfTheWhiteCouncilChoice(effectControllerId, remainingVoterIds,
                    fellowshipVoterIds, aidVotes, sourceName);
        }
    }

    /** Choice of a specific locked door for an effect that unlocks a Room for free. */
    record UnlockRoomDoorChoice(Card sourceCard, UUID controllerId, List<RoomDoor> choices)
            implements ChoiceContext {

        public UnlockRoomDoorChoice {
            choices = List.copyOf(choices);
        }

        public record RoomDoor(UUID roomPermanentId, int doorIndex, String label) {
        }
    }

    /** Choice of a door for an effect that locks an unlocked door or unlocks a locked door. */
    record LockOrUnlockRoomDoorChoice(Card sourceCard, UUID controllerId, List<RoomDoor> choices)
            implements ChoiceContext {

        public LockOrUnlockRoomDoorChoice {
            choices = List.copyOf(choices);
        }

        public record RoomDoor(UUID roomPermanentId, int doorIndex, String label) {
        }
    }

    record LibraryCastModeChoice(Card cardToCast, UUID controllerId, ChooseOneEffect effect,
                                 StackEntryType spellType, List<Integer> modeIndices,
                                 Integer discoverValue, Zone sourceZone) implements ChoiceContext {

        public LibraryCastModeChoice(Card cardToCast, UUID controllerId, ChooseOneEffect effect,
                                     StackEntryType spellType, List<Integer> modeIndices,
                                     Integer discoverValue) {
            this(cardToCast, controllerId, effect, spellType, modeIndices, discoverValue, Zone.LIBRARY);
        }

        public LibraryCastModeChoice {
            modeIndices = List.copyOf(modeIndices);
        }

        public LibraryCastModeChoice(Card cardToCast, UUID controllerId, ChooseOneEffect effect,
                                     StackEntryType spellType, List<Integer> modeIndices) {
            this(cardToCast, controllerId, effect, spellType, modeIndices, null);
        }
    }

    record ExileFreeCastModeChoice(Card cardToCast, UUID controllerId, ChooseOneEffect effect,
                                   StackEntryType spellType, List<Integer> chosenModeIndices,
                                   List<Integer> offeredModeIndices, int maximumChoices,
                                   boolean copy, boolean payManaCost) implements ChoiceContext {

        public ExileFreeCastModeChoice(Card cardToCast, UUID controllerId, ChooseOneEffect effect,
                                       StackEntryType spellType, List<Integer> chosenModeIndices,
                                       List<Integer> offeredModeIndices, int maximumChoices,
                                       boolean copy) {
            this(cardToCast, controllerId, effect, spellType, chosenModeIndices,
                    offeredModeIndices, maximumChoices, copy, false);
        }

        public ExileFreeCastModeChoice {
            chosenModeIndices = List.copyOf(chosenModeIndices);
            offeredModeIndices = List.copyOf(offeredModeIndices);
        }
    }

    /** Chooses which half of a split card a hand or graveyard free cast casts (CR 709.3). */
    record SplitFreeCastHalfChoice(SplitCardCast cast, ChooseOneEffect effect,
                                   List<Integer> offeredModeIndices) implements ChoiceContext {

        public SplitFreeCastHalfChoice {
            offeredModeIndices = List.copyOf(offeredModeIndices);
        }
    }

    /** Chooses whether a split half cast for free takes another of its optional targets. */
    /** Announces the value of X for a split card half whose cost has {X}, before its targets (CR 601.2b). */
    record SplitCastXValueChoice(SplitCardCast.Targeting targeting) implements ChoiceContext {
    }

    record SplitFreeCastMoreTargetsChoice(SplitCardCast.Targeting targeting,
                                          List<UUID> candidates) implements ChoiceContext {

        public SplitFreeCastMoreTargetsChoice {
            candidates = List.copyOf(candidates);
        }
    }

    record TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                UUID sourcePermanentId, boolean modesResetEachTurn, boolean consumeModes,
                                List<ChooseOneEffect.ChooseOneOption> chosenModes,
                                UUID triggeringCardId, UUID attackedTargetId,
                                UUID triggeringPermanentId,
                                boolean rememberLastChosenMode) implements ChoiceContext {

        public TriggeredModalChoice {
            chosenModes = List.copyOf(chosenModes);
        }

        public TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                    UUID sourcePermanentId) {
            this(sourceCard, controllerId, effect, sourcePermanentId, false, false, List.of(), null, null, null, false);
        }

        public TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                    UUID sourcePermanentId, boolean modesResetEachTurn) {
            this(sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn, false, List.of(), null, null, null, false);
        }

        public TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                    UUID sourcePermanentId, boolean modesResetEachTurn, boolean consumeModes,
                                    List<ChooseOneEffect.ChooseOneOption> chosenModes) {
            this(sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn, consumeModes,
                    chosenModes, null, null, null, false);
        }

        public TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                    UUID sourcePermanentId, boolean modesResetEachTurn,
                                    List<ChooseOneEffect.ChooseOneOption> chosenModes, UUID triggeringCardId) {
            this(sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn, false,
                    chosenModes, triggeringCardId, null, null, false);
        }

    }

    record RedistributePlayerLifeTotalsChoice(Map<String, Map<UUID, Integer>> choices) implements ChoiceContext {

        public RedistributePlayerLifeTotalsChoice {
            Map<String, Map<UUID, Integer>> copy = new java.util.LinkedHashMap<>();
            choices.forEach((label, totals) -> copy.put(label, Map.copyOf(totals)));
            choices = java.util.Collections.unmodifiableMap(copy);
        }
    }

    /**
     * Torment of Hailfire: {@code affectedPlayerId} (an opponent) chooses one of "sacrifice a nonland
     * permanent", "discard a card", or "lose N life" for one iteration of the effect. The offered
     * options are pruned to what the player can actually do (life is always offered); the "lose life"
     * label is dynamic ({@code "Lose N life"}), so a chosen value that is neither {@link #SACRIFICE}
     * nor {@link #DISCARD} means the life-loss outcome. Answered via {@code handleListChoice}.
     */
    record TormentPenaltyChoice(UUID affectedPlayerId, String sourceCardName) implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a nonland permanent";
        public static final String DISCARD = "Discard a card";

        public static String discard(int count) {
            return count == 1 ? DISCARD : "Discard " + count + " cards";
        }

        public static String sacrifice(String description) {
            return "Sacrifice " + description;
        }
    }

    /**
     * Forbidden Ritual: the targeted opponent chooses one of "sacrifice a permanent", "discard a
     * card", or "lose N life" for one cycle. Offered options are pruned to what the player can do
     * (life is always offered); lands are legal sacrifices (unlike {@link TormentPenaltyChoice}).
     * The "lose life" label is dynamic ({@code "Lose N life"}), so a chosen value that is neither
     * {@link #SACRIFICE} nor {@link #DISCARD} means the life-loss outcome. Answered via
     * {@code handleListChoice}.
     */
    record ForbiddenRitualPenaltyChoice(UUID affectedPlayerId, String sourceCardName) implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a permanent";
        public static final String DISCARD = "Discard a card";
    }

    /**
     * Oath of Lim-Dûl: for each 1 life lost, the controller chooses to sacrifice a permanent other
     * than the source enchantment, or discard a card. Offered options are pruned to what they can
     * do; if neither is possible the iteration is skipped (no prompt). Answered via
     * {@code handleListChoice}.
     */
    record OathOfLimDulPenaltyChoice(UUID affectedPlayerId, String sourceCardName) implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a permanent other than this enchantment";
        public static final String DISCARD = "Discard a card";
    }

    /**
     * Each-player discard-or-sacrifice effect: the affected player chooses to sacrifice a permanent
     * or discard a card. Offered options are pruned to what they can do.
     */
    record EachPlayerSacrificeOrDiscardChoice(UUID affectedPlayerId, String sourceCardName)
            implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a permanent";
        public static final String DISCARD = "Discard a card";
    }

    /** Each-player choice between sacrificing a matching permanent and losing life. */
    record EachPlayerSacrificeOrLoseLifeChoice(UUID affectedPlayerId, String sourceCardName)
            implements ChoiceContext {

        public static String sacrifice(String description) {
            return "Sacrifice " + description;
        }

        public static String loseLife(int amount) {
            return "Lose " + amount + " life";
        }
    }

    /** Dr. Eggman's villainous choice between discarding and letting its controller put a card in. */
    record VillainousChoice(UUID affectedPlayerId, String sourceCardName, String putOption)
            implements ChoiceContext {

        public static final String DISCARD = "Discard a card";
        public static final String DRAW = "You draw a card";
    }

    /**
     * Winter's Chill: the controller of {@code targetPermanentId} chooses pay {2}, pay {1}, or pay
     * nothing for that attacking creature. Offered options are pruned to what the player can
     * afford from their mana pool; "Pay nothing" is always offered. Answered via
     * {@code handleListChoice}.
     */
    record WintersChillPaymentChoice(UUID affectedPlayerId, UUID targetPermanentId, String sourceCardName)
            implements ChoiceContext {

        public static final String PAY_TWO = "Pay {2}";
        public static final String PAY_ONE = "Pay {1}";
        public static final String PAY_NOTHING = "Pay nothing";
    }

    /** Disorienting Choice: the controller of each chosen permanent may exile it. */
    record DisorientingChoiceExileChoice(UUID affectedPlayerId, UUID targetPermanentId,
                                          String sourceCardName) implements ChoiceContext {

        public static final String EXILE = "Exile it";
        public static final String KEEP = "Keep it";
    }

    /**
     * Forgotten Lore and Shrouded Lore: after the targeted opponent has chosen a card in the
     * controller's graveyard, the controller chooses whether to pay the configured mana cost and
     * repeat the process. Only offered when the controller can afford it and at least one unchosen
     * card remains. Answered via {@code handleListChoice}.
     */
    record ForgottenLorePaymentChoice(UUID affectedPlayerId, String sourceCardName, String repeatManaCost)
            implements ChoiceContext {

        public static final String PAY = "Pay {G}";
        public static final String DECLINE = "Don't pay";

        public static String payOption(String repeatManaCost) {
            return "Pay " + repeatManaCost;
        }
    }

    /** Chooses how to pay, or not pay, an enchanted permanent's upkeep penalty. */
    record EnchantedPermanentManaOrLifePaymentChoice(
            UUID affectedPlayerId, String sourceCardName, UUID sourcePermanentId,
            String manaCost, int lifeCost) implements ChoiceContext {

        public static final String DECLINE = "Don't pay";

        public String payManaOption() {
            return "Pay " + manaCost;
        }

        public String payLifeOption() {
            return "Pay " + lifeCost + " life";
        }
    }

    /**
     * Chooses how to pay, or not pay, "that player may pay {M} or N life" (Emberwilde Djinn). Offered
     * only when the player can afford both halves; otherwise the plain yes/no prompt is enough.
     */
    record TargetPlayerManaOrLifePaymentChoice(
            UUID payerId, String sourceCardName, String manaCost, int lifeCost) implements ChoiceContext {

        public static final String DECLINE = "Don't pay";

        public String payManaOption() {
            return "Pay " + manaCost;
        }

        public String payLifeOption() {
            return "Pay " + lifeCost + " life";
        }
    }

    /** Indulgent Tormentor: the targeted opponent chooses sacrifice, payment, or a draw. */
    record IndulgentTormentorChoice(UUID affectedPlayerId, int lifeCost, String sourceCardName)
            implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a creature";
        public static final String DRAW = "Draw a card";

        public static String payLife(int lifeCost) {
            return "Pay " + lifeCost + " life";
        }
    }


    record TimeTravelActionChoice(UUID controllerId, String sourceCardName,
                                  List<TimeTravelTarget> targets, int targetIndex,
                                  int remainingTravels,
                                  List<TimeTravelTargetChoice> choicesSoFar) implements ChoiceContext {

        public static final String ADD = "ADD";
        public static final String REMOVE = "REMOVE";
        public static final String SKIP = "SKIP";
        public static final List<String> OPTIONS = List.of(ADD, REMOVE, SKIP);

        public TimeTravelActionChoice {
            targets = List.copyOf(targets);
            choicesSoFar = List.copyOf(choicesSoFar);
        }

        public TimeTravelTarget target() {
            return targets.get(targetIndex);
        }
    }

    record TimeTravelTarget(UUID id, Zone zone) {
    }

    /** One object's add/remove decision within a single time-travel event, applied once every decision is made. */
    record TimeTravelTargetChoice(TimeTravelTarget target, String choice) {
    }

    record AddAnotherCounterTypeOnEachNonSagaPermanentChoice(
            UUID targetId, UUID controllerId, String sourceCardName,
            List<UUID> remainingTargetIds, List<CounterType> counterTypes) implements ChoiceContext {

        public static final String SKIP = "SKIP";

        public AddAnotherCounterTypeOnEachNonSagaPermanentChoice {
            remainingTargetIds = List.copyOf(remainingTargetIds);
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            List<String> options = new java.util.ArrayList<>(counterTypes.stream()
                    .map(AddAnotherCounterTypeOnEachNonSagaPermanentChoice::counterLabel)
                    .toList());
            options.add(SKIP);
            return options;
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    record VoteForInnocentOrGuiltyChoice(UUID effectControllerId, List<UUID> remainingPlayerIds,
                                         Map<String, Integer> votes, String sourceName)
            implements ChoiceContext, VoteChoiceContext {
        public static final String INNOCENT = "Innocent";
        public static final String GUILTY = "Guilty";
        public static final List<String> OPTIONS = List.of(INNOCENT, GUILTY);

        public VoteForInnocentOrGuiltyChoice {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            votes = Map.copyOf(votes);
        }

        @Override
        public List<UUID> remainingVoterIds() {
            return remainingPlayerIds;
        }

        @Override
        public VoteForInnocentOrGuiltyChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new VoteForInnocentOrGuiltyChoice(effectControllerId, remainingVoterIds,
                    votes, sourceName);
        }
    }

    record ToymakersTrapChoice(UUID controllerId, UUID sourcePermanentId, Card sourceCard,
                               UUID opponentId, int chosenNumber, boolean guess)
            implements ChoiceContext {}
}
