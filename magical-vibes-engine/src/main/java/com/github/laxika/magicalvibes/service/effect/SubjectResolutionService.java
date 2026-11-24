package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenParityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasCardTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithCardsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithCardsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceOrPairedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.OutsideGameCards;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The only code that turns a {@link PermanentSubject}, {@link PlayerSubject} or {@link CardSubject} into permanents,
 * players or cards. A handler
 * names its subject and keeps no lookup of its own. Two entry points:
 *
 * <ul>
 *   <li>for a resolving effect, {@code (GameData, StackEntry, CardEffect, subject)};</li>
 *   <li>for a static ability, {@code (GameData, source Permanent, subject)}: a subject that needs a stack entry
 *       ({@link PermanentSubject#needsStackEntry()}) is rejected there.</li>
 * </ul>
 *
 * <p>The rules it applies:
 * <ol>
 *   <li>Only a target variant names the effect's targets; the target is chosen as the object goes on the stack
 *       (CR 115.1) and an illegal one was already dropped from the entry (CR 608.2b). No other subject makes anything
 *       fizzle.</li>
 *   <li>A reference to an object reads its current information while it is on the battlefield, else its last-known
 *       information (CR 608.2h): "its controller" of a creature that died is the controller it last had.</li>
 *   <li>{@link #permanents} returns only permanents still on the battlefield: nothing is done to one that left
 *       (CR 609.3), and one that left and came back is a new object (CR 400.7). {@link #permanentIds} also names the
 *       ones that left, for a verb whose rules act on last-known information (an explore, CR 701.44c).</li>
 *   <li>A set ({@link PermanentSubject.ControlledBy}) is found once, when the effect is applied (CR 608.2h).</li>
 *   <li>Several players are returned in APNAP order (CR 101.4): the active player, then the others in turn order.</li>
 * </ol>
 *
 * <p>{@code source()} is the entry's source permanent and nothing else: an entry with no source permanent (a spell, a
 * cast trigger) has none, never its target. An owner the card doesn't record (a hand-built or engine-made card) is
 * the player the permanent entered under (CR 110.2, 111.2): {@code GameData.defaultControllerOf} while it is on the
 * battlefield, its last-known controller once it has left (for the source of the resolving ability, that ability's
 * controller when nothing else recorded it).
 */
@Service
@RequiredArgsConstructor
public class SubjectResolutionService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;

    /** The permanents {@code subject} names that are on the battlefield, for a resolving effect. */
    public List<Permanent> permanents(GameData gameData, StackEntry entry, CardEffect effect,
                                      PermanentSubject subject) {
        return livePermanents(new Context(gameData, entry, effect, null, entry.getControllerId()), subject);
    }

    /** The first permanent {@code subject} names that is on the battlefield, or {@code null}. */
    public Permanent permanent(GameData gameData, StackEntry entry, CardEffect effect, PermanentSubject subject) {
        List<Permanent> permanents = permanents(gameData, entry, effect, subject);
        return permanents.isEmpty() ? null : permanents.getFirst();
    }

    /**
     * The ids of the permanents {@code subject} names, those that have left the battlefield included, for a verb that
     * acts on last-known information.
     */
    public List<UUID> permanentIds(GameData gameData, StackEntry entry, CardEffect effect, PermanentSubject subject) {
        return ids(new Context(gameData, entry, effect, null, entry.getControllerId()), subject);
    }

    /**
     * The spell a spell subject names while it is on the stack, or {@code null}: for {@code Subjects.triggeringSpell()}
     * the spell the trigger event was about, for {@code Subjects.targetSpell()} the spell among the entry's targets (of
     * any target group, so "target spell" of a spell that also targets a creature).
     */
    public StackEntry spellOnStack(GameData gameData, StackEntry entry, PermanentSubject spell) {
        if (spell instanceof PermanentSubject.TriggeringSpell) {
            UUID spellCardId = entry.getTriggeringCardId();
            return spellCardId == null ? null : gameData.stack.stream()
                    .filter(candidate -> candidate != entry && spellCardId.equals(candidate.getTargetableId()))
                    .findFirst()
                    .orElse(null);
        }
        if (!(spell instanceof PermanentSubject.TargetSpell)) {
            throw new IllegalArgumentException("Not a spell subject: " + spell);
        }
        List<UUID> candidates = new ArrayList<>();
        if (entry.getTargetId() != null) {
            candidates.add(entry.getTargetId());
        }
        Card targeting = entry.getTargetingCard();
        int groups = targeting == null ? 0 : targeting.getTargetGroups().size();
        for (int group = 0; group < groups; group++) {
            candidates.addAll(entry.targetsForGroup(group));
        }
        candidates.addAll(entry.getTargetIds());
        for (UUID candidate : candidates) {
            for (StackEntry onStack : gameData.stack) {
                if (onStack != entry && candidate.equals(onStack.getTargetableId())) {
                    return onStack;
                }
            }
        }
        return null;
    }

    /**
     * The ids of the sources of damage {@code subject} names for a resolving effect (CR 609.7a): as
     * {@link #permanentIds}, a chosen source that is a spell or another object included by the id the choice recorded,
     * and the target spell ({@code Subjects.targetSpell()}) by its card's id while it is on the stack.
     */
    public List<UUID> damageSourceIds(GameData gameData, StackEntry entry, CardEffect effect,
                                      PermanentSubject subject) {
        if (subject instanceof PermanentSubject.TargetSpell) {
            UUID spellCardId = entry.getTargetId();
            return spellCardId != null && gameData.stack.stream()
                    .anyMatch(onStack -> spellCardId.equals(onStack.getTargetableId()))
                    ? List.of(spellCardId) : List.of();
        }
        return permanentIds(gameData, entry, effect, subject);
    }

    /** The players {@code subject} names, in APNAP order, for a resolving effect. */
    public List<UUID> players(GameData gameData, StackEntry entry, CardEffect effect, PlayerSubject subject) {
        return players(new Context(gameData, entry, effect, null, entry.getControllerId()), subject);
    }

    /** The first player {@code subject} names, or {@code null}. */
    public UUID player(GameData gameData, StackEntry entry, CardEffect effect, PlayerSubject subject) {
        List<UUID> players = players(gameData, entry, effect, subject);
        return players.isEmpty() ? null : players.getFirst();
    }

    /**
     * The cards {@code subject} names for a resolving effect, as they are now: each in the public zone it is expected
     * to be in. A card that moved from that zone since is a new object (CR 400.7) and is not named; a card a step of
     * the same resolution moved to a public zone is found there (CR 400.7j).
     */
    public List<Card> cards(GameData gameData, StackEntry entry, CardEffect effect, CardSubject subject) {
        return locatedCards(gameData, entry, effect, subject).stream().map(LocatedCard::card).toList();
    }

    /** {@link #cards}, each with the zone it is in and the player whose zone that is. */
    public List<LocatedCard> locatedCards(GameData gameData, StackEntry entry, CardEffect effect,
                                          CardSubject subject) {
        return switch (subject) {
            case CardSubject.CardsThisWay thisWay -> thisWay.filter() == null
                    ? cardsThisWay(gameData, entry, thisWay.kind())
                    : cardsThisWay(gameData, entry, thisWay.kind()).stream()
                    .filter(located -> cardMatches(gameData, entry, located.card(), located.ownerId(),
                            thisWay.filter()))
                    .toList();
            case CardSubject.TargetCard target -> targetCard(gameData, entry, target);
            case CardSubject.TriggeringCards triggering -> triggering.asLastKnown()
                    ? triggeringCardsAsLastKnown(gameData, entry, triggering)
                    : triggeringCards(gameData, entry, triggering);
            case CardSubject.TriggeringCardsInHand ignored -> eventCardsPutIntoHand(entry).stream()
                    .map(card -> locate(gameData, card.getId(), Zone.HAND))
                    .filter(Objects::nonNull)
                    .toList();
            case CardSubject.SourceCard ignored -> sourceCard(gameData, entry);
            case CardSubject.InGraveyards in -> inGraveyards(gameData, entry, effect, in);
            case CardSubject.InExile in -> inExile(gameData, entry, effect, in);
            case CardSubject.ExiledAsCost ignored -> exiledAsCost(gameData, entry);
            case CardSubject.ExiledWithSource with -> exiledWithSource(gameData, entry, effect, with);
            case CardSubject.Except except -> {
                Set<UUID> excluded = new LinkedHashSet<>();
                locatedCards(gameData, entry, effect, except.excluded()).forEach(out -> excluded.add(out.card().getId()));
                yield locatedCards(gameData, entry, effect, except.cards()).stream()
                        .filter(card -> !excluded.contains(card.card().getId())).toList();
            }
            case CardSubject.TargetExiledCard target -> targetExiledCard(gameData, entry, effect, target);
            case CardSubject.TopOfGraveyard top -> topOfGraveyard(gameData, entry, effect, top);
            case CardSubject.InHand in -> inZone(gameData, entry, effect, in.owners(), in.filter(), Zone.HAND,
                    gameData.playerHands);
            case CardSubject.InLibrary in -> inZone(gameData, entry, effect, in.owners(), in.filter(), Zone.LIBRARY,
                    gameData.playerDecks);
            case CardSubject.TopmostInLibrary top -> topmostInLibrary(gameData, entry, effect, top);
            case CardSubject.InCommandZone in -> inZone(gameData, entry, effect, in.owners(), in.filter(), Zone.COMMAND,
                    gameData.playerCommandZones);
            case CardSubject.Voted voted -> voted(entry, StepOutcome.Vote.CARD, voted.tally()).stream()
                    .map(cardId -> {
                        LocatedCard inGraveyard = locate(gameData, cardId, Zone.GRAVEYARD);
                        return inGraveyard != null ? inGraveyard : locate(gameData, cardId, Zone.EXILE);
                    })
                    .filter(Objects::nonNull)
                    .toList();
            case CardSubject.Owned owned -> owned(gameData, entry, effect, owned);
            case CardSubject.OutsideTheGame outside -> outsideTheGame(gameData, entry, effect, outside);
            case CardSubject.InYourGraveyard yours -> locatedCards(gameData, entry, effect, yours.cards()).stream()
                    .filter(located -> located.zone() == Zone.GRAVEYARD
                            && entry.getControllerId().equals(located.ownerId()))
                    .toList();
            case CardSubject.OwnedBy owned -> {
                List<UUID> owners = players(gameData, entry, effect, owned.owners());
                yield locatedCards(gameData, entry, effect, owned.cards()).stream()
                        .filter(located -> owners.contains(located.ownerId()))
                        .toList();
            }
        };
    }

    /**
     * {@code filter} with each {@link CardSharesCardTypeWithCardsPredicate} bound to the card types the cards its subject
     * names have as the effect applies ("a card that doesn't share a card type with the discarded card"): it becomes
     * "is any of those types", which matches nothing when the subject names no card; and each
     * {@link CardSharesNameWithPermanentsPredicate} to the names those permanents have ({@link #names}), which matches
     * nothing when they have none; each {@link CardSharesCardTypeWithPermanentsPredicate} to their card types
     * ({@link #cardTypes}); each {@link CardSharesCreatureTypeWithPermanentsPredicate} to their creature types
     * ({@link #creatureTypes}); and each {@link CardSharesNameWithCardsPredicate} to the names of the cards its subject
     * names. A filter without the predicates is returned as it is.
     */
    public CardPredicate bindCardSubjects(GameData gameData, StackEntry entry, CardEffect effect, CardPredicate filter) {
        return switch (filter) {
            case null -> null;
            case CardSharesCardTypeWithCardsPredicate shares -> {
                Set<CardType> types = EnumSet.noneOf(CardType.class);
                for (LocatedCard located : locatedCards(gameData, entry, effect, shares.cards())) {
                    for (CardType type : CardType.values()) {
                        if (gameQueryService.cardHasType(located.card(), type, gameData, located.ownerId())) {
                            types.add(type);
                        }
                    }
                }
                yield new CardAnyOfPredicate(types.stream()
                        .<CardPredicate>map(CardTypePredicate::new).toList());
            }
            case CardSharesNameWithPermanentsPredicate shares -> new CardAnyOfPredicate(
                    names(gameData, entry, effect, shares.permanents()).stream()
                            .<CardPredicate>map(CardNamedPredicate::new).toList());
            case CardSharesCardTypeWithPermanentsPredicate shares -> new CardAnyOfPredicate(
                    cardTypes(gameData, entry, effect, shares.permanents()).stream()
                            .<CardPredicate>map(CardTypePredicate::new).toList());
            // A changeling card is every creature type (CR 702.73a), which CardSubtypePredicate counts.
            case CardSharesCreatureTypeWithPermanentsPredicate shares -> new CardAnyOfPredicate(
                    creatureTypes(gameData, entry, effect, shares.permanents()).stream()
                            .<CardPredicate>map(CardSubtypePredicate::new).toList());
            case CardSharesNameWithCardsPredicate shares -> {
                Set<String> cardNames = new LinkedHashSet<>();
                for (LocatedCard located : locatedCards(gameData, entry, effect, shares.cards())) {
                    if (located.card().getName() != null && !located.card().getName().isEmpty()) {
                        cardNames.addAll(located.card().getNames());
                    }
                }
                yield new CardAnyOfPredicate(cardNames.stream().<CardPredicate>map(CardNamedPredicate::new).toList());
            }
            case CardAllOfPredicate all -> new CardAllOfPredicate(all.predicates().stream()
                    .map(part -> bindCardSubjects(gameData, entry, effect, part)).toList());
            case CardAnyOfPredicate any -> new CardAnyOfPredicate(any.predicates().stream()
                    .map(part -> bindCardSubjects(gameData, entry, effect, part)).toList());
            case CardNotPredicate not -> new CardNotPredicate(bindCardSubjects(gameData, entry, effect, not.predicate()));
            default -> filter;
        };
    }

    /**
     * {@code filter} with each {@link PermanentSharesCreatureTypeWithPermanentsPredicate} bound to the creature types the
     * permanents its subject names have as the effect applies ({@link #creatureTypes}, CR 608.2h): it becomes "has any of
     * those creature types", which matches nothing when they have none ("destroy all creatures that share a creature
     * type with the sacrificed creature", Endemic Plague). A filter without the predicate is returned as it is.
     */
    public PermanentPredicate bindPermanentSubjects(GameData gameData, StackEntry entry, CardEffect effect,
                                                    PermanentPredicate filter) {
        return switch (filter) {
            case null -> null;
            // A changeling permanent is every creature type (CR 702.73a), which PermanentHasSubtypePredicate counts.
            case PermanentSharesCreatureTypeWithPermanentsPredicate shares -> new PermanentAnyOfPredicate(
                    creatureTypes(gameData, entry, effect, shares.permanents()).stream()
                            .<PermanentPredicate>map(PermanentHasSubtypePredicate::new).toList());
            case PermanentAllOfPredicate all -> new PermanentAllOfPredicate(all.predicates().stream()
                    .map(part -> bindPermanentSubjects(gameData, entry, effect, part)).toList());
            case PermanentAnyOfPredicate any -> new PermanentAnyOfPredicate(any.predicates().stream()
                    .map(part -> bindPermanentSubjects(gameData, entry, effect, part)).toList());
            case PermanentNotPredicate not -> new PermanentNotPredicate(
                    bindPermanentSubjects(gameData, entry, effect, not.predicate()));
            default -> filter;
        };
    }

    /**
     * {@code filter} with every part that needs the resolving entry bound as the effect applies (CR 608.2h): the card
     * subjects ({@link #bindCardSubjects}: "a different name than that spell" over {@code targetSpell()}) and the
     * triggering spell's card types, name and mana value ({@code AmountEvaluationService.bindTriggeringSpellCardTypes},
     * {@code bindTriggeringSpellName}, {@code bindTriggeringSpellManaValue}: "a card that shares a card type with it"),
     * the spell as it is on the stack, else as it last existed there. A filter without them is returned as it is.
     */
    public CardPredicate bindResolutionFilter(GameData gameData, StackEntry entry, CardEffect effect,
                                              CardPredicate filter) {
        CardPredicate bound = bindCardSubjects(gameData, entry, effect, filter);
        bound = AmountEvaluationService.bindTriggeringSpellCardTypes(gameData, bound, entry);
        bound = AmountEvaluationService.bindTriggeringSpellName(gameData, bound, entry);
        return AmountEvaluationService.bindTriggeringSpellManaValue(gameData, bound, entry);
    }

    /**
     * {@code filter} with each value the resolution chose, or the source chose as it entered, written in as the effect
     * applies, so a rule or delayed ability the effect registers keeps it when it is judged later without the entry
     * (CR 608.2h, 611.2c): {@link PermanentHasTypeChosenThisResolutionPredicate} becomes "has that subtype" ("creatures
     * of the creature type of your choice attack this turn if able", Walking Desecration);
     * {@link PermanentHasChosenParityPredicate} of mana value "has mana value of that parity" (Twins of Discord; the power
     * form stays as it is, judged while the entry resolves);
     * {@link PermanentControlledByPlayersPredicate} "controlled by one of those players", its players found now ("creatures
     * that player controls", The Black Gate). A leaf with nothing chosen, or no player, matches nothing. A filter
     * without them is returned as it is.
     */
    public PermanentPredicate bindChosenValues(GameData gameData, StackEntry entry, CardEffect effect,
                                               PermanentPredicate filter) {
        return switch (filter) {
            case null -> null;
            case PermanentHasTypeChosenThisResolutionPredicate ignored -> entry.getChosenCreatureType() == null
                    ? new PermanentNotPredicate(new PermanentTruePredicate())
                    : new PermanentHasSubtypePredicate(entry.getChosenCreatureType());
            case PermanentHasChosenParityPredicate parity when parity.of() == PermanentHasChosenParityPredicate.Quality.MANA_VALUE ->
                    entry.getChosenParity() == null
                            ? new PermanentNotPredicate(new PermanentTruePredicate())
                            : new PermanentManaValueParityPredicate(entry.getChosenParity());
            case PermanentControlledByPlayersPredicate controlled -> {
                List<UUID> players = players(gameData, entry, effect, controlled.players());
                yield players.isEmpty() ? new PermanentNotPredicate(new PermanentTruePredicate())
                        : players.size() == 1 ? new PermanentControlledByPlayerPredicate(players.getFirst())
                        : new PermanentAnyOfPredicate(players.stream()
                                .<PermanentPredicate>map(PermanentControlledByPlayerPredicate::new).toList());
            }
            case PermanentAllOfPredicate all -> new PermanentAllOfPredicate(all.predicates().stream()
                    .map(part -> bindChosenValues(gameData, entry, effect, part)).toList());
            case PermanentAnyOfPredicate any -> new PermanentAnyOfPredicate(any.predicates().stream()
                    .map(part -> bindChosenValues(gameData, entry, effect, part)).toList());
            case PermanentNotPredicate not -> new PermanentNotPredicate(
                    bindChosenValues(gameData, entry, effect, not.predicate()));
            default -> filter;
        };
    }

    /**
     * {@code filter} with each chosen value written in as the effect applies, so the next-spell rule it registers keeps
     * it (CR 608.2h): {@link CardHasTypeChosenThisResolutionPredicate} and {@link CardHasCardTypeChosenThisResolutionPredicate}
     * become the subtype or card type this resolution chose; {@link CardHasSourceChosenSubtypePredicate} and
     * {@link CardHasSourceChosenCardTypePredicate} the one the source chose as it entered, read from the source as it
     * is, else as it last existed ("the next spell of the chosen type you cast this turn", Progenitor's Icon, Apex
     * Observatory). A leaf with nothing chosen matches nothing. A filter without them is returned as it is.
     */
    public CardPredicate bindChosenValues(GameData gameData, StackEntry entry, CardEffect effect, CardPredicate filter) {
        return switch (filter) {
            case null -> null;
            case CardHasTypeChosenThisResolutionPredicate ignored -> entry.getChosenCreatureType() == null
                    ? new CardNotPredicate(new CardTruePredicate())
                    : new CardSubtypePredicate(entry.getChosenCreatureType());
            case CardHasCardTypeChosenThisResolutionPredicate ignored -> entry.getChosenCardType() == null
                    ? new CardNotPredicate(new CardTruePredicate())
                    : new CardTypePredicate(entry.getChosenCardType());
            case CardHasSourceChosenSubtypePredicate chosen -> {
                Permanent source = sourceAsItIsOrLastWas(gameData, entry);
                CardSubtype subtype = source == null ? null : source.getChosenSubtype();
                if (subtype == null) {
                    yield new CardNotPredicate(new CardTruePredicate());
                }
                // A changeling card has every creature type (CR 702.73a), which CardSubtypePredicate counts.
                yield chosen.creatureOnly()
                        ? new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(subtype)))
                        : new CardSubtypePredicate(subtype);
            }
            case CardHasSourceChosenCardTypePredicate ignored -> {
                Permanent source = sourceAsItIsOrLastWas(gameData, entry);
                CardType type = source == null ? null : source.getChosenCardType();
                yield type == null ? new CardNotPredicate(new CardTruePredicate()) : new CardTypePredicate(type);
            }
            case CardAllOfPredicate all -> new CardAllOfPredicate(all.predicates().stream()
                    .map(part -> bindChosenValues(gameData, entry, effect, part)).toList());
            case CardAnyOfPredicate any -> new CardAnyOfPredicate(any.predicates().stream()
                    .map(part -> bindChosenValues(gameData, entry, effect, part)).toList());
            case CardNotPredicate not -> new CardNotPredicate(bindChosenValues(gameData, entry, effect, not.predicate()));
            default -> filter;
        };
    }

    /** The entry's source permanent as it is, else as it last existed (CR 608.2h); {@code null} without one. */
    private Permanent sourceAsItIsOrLastWas(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null ? source : entry.getSourcePermanentSnapshot();
    }

    /**
     * The names of what {@code subject} names, for "the same name as that permanent": each permanent's as it is on
     * the battlefield, else as the exile step of this resolution recorded it (CR 608.2h); for {@code targetSpell()}
     * the spell's as it is on the stack, else as the counter step remembered it. An object with no name (face down,
     * CR 708.2) adds none, so shares a name with nothing (CR 201.2a).
     */
    public Set<String> names(GameData gameData, StackEntry entry, CardEffect effect, PermanentSubject subject) {
        Set<String> names = new LinkedHashSet<>();
        if (subject instanceof PermanentSubject.TargetSpell) {
            UUID spellId = entry.getTargetId();
            if (gameData.stack.stream().anyMatch(onStack -> onStack.isCastFaceDown()
                    && onStack.getTargetableId().equals(spellId))) {
                // A face-down spell still on the stack has no name (CR 708.2).
                return names;
            }
            Card spell = gameData.stack.stream()
                    .filter(onStack -> spellId != null && spellId.equals(onStack.getTargetableId()))
                    .map(StackEntry::getCard)
                    .findFirst()
                    .orElseGet(() -> entry.lastKnownPermanentCard(spellId));
            if (spell != null && spell.getName() != null && !spell.getName().isEmpty()) {
                names.addAll(spell.getNames());
            }
            return names;
        }
        StepOutcome exiled = entry.getResolutionResults().outcome(StepKind.EXILE);
        for (UUID id : permanentIds(gameData, entry, effect, subject)) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            String name = permanent != null ? gameQueryService.getEffectiveName(gameData, permanent)
                    : exiled.lastKnownPermanent(id).map(LastKnownCardStats::name).orElse(null);
            if (name != null && !name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * The card types of what {@code subject} names, for "shares a card type with the sacrificed permanent", "with that
     * permanent": each permanent's as it is on the battlefield, else as a removal step of this resolution recorded it
     * as it left ({@code DepartureResult.lastKnownCardTypes}), else as the entry's own snapshot of it last had them
     * (its triggering, sacrificed, attached or source permanent; CR 608.2h).
     */
    public Set<CardType> cardTypes(GameData gameData, StackEntry entry, CardEffect effect, PermanentSubject subject) {
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        for (UUID id : permanentIds(gameData, entry, effect, subject)) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                types.addAll(gameQueryService.getEffectiveCardTypes(gameData, permanent));
                continue;
            }
            entry.getResolutionResults().departures().stream()
                    .filter(departure -> departure.permanentId().equals(id))
                    .findFirst()
                    .ifPresentOrElse(departure -> types.addAll(departure.lastKnownCardTypes()), () -> {
                        Permanent snapshot = lastKnownSnapshot(gameData, entry, id);
                        if (snapshot != null) {
                            types.addAll(gameQueryService.getEffectiveCardTypes(gameData, snapshot));
                        }
                    });
        }
        return types;
    }

    /**
     * The creature types of what {@code subject} names, for "shares a creature type with the sacrificed creature": each
     * permanent's as it is on the battlefield, else as it last existed there (CR 608.2h), read from the entry's snapshot
     * of it (the sacrificed, triggering, attached or source permanent), a removal step's
     * {@code DepartureResult.lastKnownSubtypes} or the game's departure snapshot. A changeling is every creature type
     * (CR 702.73a); a face-down permanent has only the creature types effects give it (CR 708.2a).
     */
    public Set<CardSubtype> creatureTypes(GameData gameData, StackEntry entry, CardEffect effect,
                                          PermanentSubject subject) {
        Set<CardSubtype> types = EnumSet.noneOf(CardSubtype.class);
        for (UUID id : permanentIds(gameData, entry, effect, subject)) {
            Permanent live = gameQueryService.findPermanentById(gameData, id);
            Permanent permanent = live != null ? live : lastKnownSnapshot(gameData, entry, id);
            if (permanent != null) {
                if (live == null && permanent.isFaceDown() && permanent.getLastKnownSubtypes() == null) {
                    // A face-down snapshot that recorded no subtypes would read its hidden card's (CR 708.2a).
                    continue;
                }
                if (!permanent.isFaceDown() && gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)) {
                    addEveryCreatureType(types);
                } else {
                    types.addAll(gameQueryService.effectiveCreatureSubtypes(gameData, permanent));
                }
                continue;
            }
            entry.getResolutionResults().departures().stream()
                    .filter(departure -> departure.permanentId().equals(id))
                    .findFirst()
                    .ifPresent(departure -> departure.lastKnownSubtypes().stream()
                            .filter(gameQueryService::isCreatureSubtype)
                            .forEach(types::add));
        }
        return types;
    }

    private void addEveryCreatureType(Set<CardSubtype> types) {
        for (CardSubtype subtype : CardSubtype.values()) {
            if (gameQueryService.isCreatureSubtype(subtype)) {
                types.add(subtype);
            }
        }
    }

    /**
     * The entry's own record of the permanent {@code permanentId} as it last existed on the battlefield: its sacrificed,
     * triggering, attached or source permanent snapshot, else the game's departure snapshot, else a permanent made from
     * the card the entry last knew it as; {@code null} when none names it.
     */
    private Permanent lastKnownSnapshot(GameData gameData, StackEntry entry, UUID permanentId) {
        for (Permanent snapshot : java.util.Arrays.asList(entry.getSacrificedPermanentSnapshot(),
                entry.getTriggeringPermanentSnapshot(), entry.getAttachedPermanentSnapshot(),
                entry.getSourcePermanentSnapshot())) {
            if (snapshot != null && permanentId.equals(snapshot.getId())) {
                return snapshot;
            }
        }
        if (entry.getResolutionResults().departures().stream()
                .anyMatch(departure -> departure.permanentId().equals(permanentId))) {
            return null;
        }
        Permanent departed = gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> permanentId.equals(snapshot.getId()))
                .findFirst().orElse(null);
        if (departed != null) {
            return departed;
        }
        Card lastKnownCard = entry.lastKnownPermanentCard(permanentId);
        return lastKnownCard == null ? null : new Permanent(lastKnownCard);
    }

    /**
     * The card of each object {@code subject} names, as a copy of that object reads it, for "a duplicate of it": a
     * permanent's as it is on the battlefield, else as the entry last knew it (CR 608.2h), with its face-down values
     * while it is or last was face down (CR 708.2a); for {@code triggeringSpell()} the spell's whole card (its Omen,
     * Adventure or split half cast is still that card), as it is on the stack, else as it last existed there, with
     * only the characteristics a spell cast face down has (CR 708.2). A token or a copy of a spell has no
     * card (CR 111.1, 707.10) and adds none. For {@code targetSpell()}, "a duplicate of target spell": the target
     * spell's card as it is on the stack, its whole card (the Omen, Adventure or split half cast is still that card),
     * a copy's the card it was copied as, with only the characteristics of a spell cast face down (CR 708.2).
     */
    public List<Card> cardsToDuplicate(GameData gameData, StackEntry entry, CardEffect effect,
                                       PermanentSubject subject) {
        List<Card> cards = new ArrayList<>();
        if (subject instanceof PermanentSubject.TargetSpell) {
            UUID spellId = entry.getTargetId();
            StackEntry spell = gameData.stack.stream()
                    .filter(onStack -> spellId != null && spellId.equals(onStack.getTargetableId()))
                    .findFirst()
                    .orElse(null);
            if (spell != null) {
                Card card = spell.isCastFaceDown() ? CastingPermissionService.spellAsEffectsSeeIt(spell).getCard()
                        : spell.isCopy() || spell.getPhysicalCard() == null ? spell.getCard() : spell.getPhysicalCard();
                if (card != null) {
                    cards.add(card);
                }
            }
            return cards;
        }
        if (subject instanceof PermanentSubject.TriggeringSpell) {
            StackEntry spell = CopySupport.triggeringSpell(gameData, entry);
            if (spell != null && !spell.isCopy() && spell.getPhysicalCard() != null) {
                cards.add(spell.isCastFaceDown() ? CastingPermissionService.spellAsEffectsSeeIt(spell).getCard()
                        : spell.getPhysicalCard());
            }
        } else {
            for (UUID id : permanentIds(gameData, entry, effect, subject)) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                Card card = permanent != null ? PermanentCopierService.cardToCopy(permanent)
                        : PermanentCopierService.lastKnownCardToCopy(gameData, entry, id,
                        entry.lastKnownPermanentCard(id));
                if (card == null && permanent == null) {
                    // The trigger event's permanent that has died since ("whenever a creature dies, conjure a
                    // duplicate of it"), as the entry's snapshot of it last showed it.
                    Permanent snapshot = entry.getTriggeringPermanentSnapshot();
                    if (snapshot != null && id.equals(snapshot.getId())) {
                        card = PermanentCopierService.cardToCopy(snapshot);
                    }
                }
                if (card == null && permanent == null) {
                    // A permanent a batch step removed without the entry noting its card ("each artifact destroyed
                    // this way"), as the game's departure snapshot of it last showed it.
                    card = gameData.departedPermanentSnapshots.values().stream()
                            .filter(departed -> id.equals(departed.getId()))
                            .findFirst()
                            .map(PermanentCopierService::cardToCopy)
                            .orElse(null);
                }
                if (card != null) {
                    cards.add(card);
                }
            }
        }
        return cards.stream().filter(card -> !card.isToken()).toList();
    }

    /**
     * The originals of "conjure a duplicate of [those cards]", each where it is: the cards {@code subject} names, but
     * for {@link CardSubject.TriggeringCardsInHand} each card the event put into a hand as the event recorded it, also
     * once it has left that hand: a hand is hidden, so the duplicate reads the card's last known information
     * (CR 608.2h). No token, which is no card (CR 108.2b).
     */
    public List<LocatedCard> cardsToDuplicate(GameData gameData, StackEntry entry, CardEffect effect,
                                              CardSubject subject) {
        if (subject instanceof CardSubject.TriggeringCards triggering) {
            return triggeringCardsAsLastKnown(gameData, entry, triggering);
        }
        if (subject instanceof CardSubject.CardsThisWay thisWay && thisWay.kind() == StepKind.CHOOSE) {
            return chosenCardsAsLastKnown(gameData, entry, effect, thisWay);
        }
        if (!(subject instanceof CardSubject.TriggeringCardsInHand)) {
            return locatedCards(gameData, entry, effect, subject).stream()
                    .filter(located -> !located.card().isToken()).toList();
        }
        List<LocatedCard> originals = new ArrayList<>();
        for (Card card : eventCardsPutIntoHand(entry)) {
            if (card.isToken()) {
                continue;
            }
            LocatedCard inHand = locate(gameData, card.getId(), Zone.HAND);
            originals.add(inHand != null ? inHand : new LocatedCard(card, Zone.HAND, gameData.ownerOf(card)));
        }
        return originals;
    }

    /**
     * {@link CardSubject.TriggeringCards} for a duplicate or a copy: each card the event put into a graveyard (or exiled)
     * where it still is in that stay, else as the event recorded it, its last known information once it left
     * (CR 608.2h: a discarded card exiled from the graveyard, a madness card the discard exiled instead, a milled card
     * exiled in response, a card that left the graveyard). The filter is judged on the card as it is named.
     */
    private List<LocatedCard> triggeringCardsAsLastKnown(GameData gameData, StackEntry entry,
                                                         CardSubject.TriggeringCards triggering) {
        Map<UUID, LocatedCard> named = new LinkedHashMap<>();
        triggeringCards(gameData, entry, triggering).forEach(located -> named.put(located.card().getId(), located));
        List<Card> eventCards = entry.getTriggeringCards();
        if (eventCards.isEmpty() && entry.getTriggeringCardId() != null
                && entry.getTriggeringCardGraveyardEntryVersion() >= 0) {
            // The one card of an event that recorded its graveyard stay ("whenever a creature card is put into an
            // opponent's graveyard"): as the event recorded it, else the card where it went from there (exile).
            Card snapshot = entry.getTriggeringCardSnapshot();
            ExiledCardEntry exiled = gameData.findExiledCard(entry.getTriggeringCardId());
            Card known = snapshot != null && snapshot.getId().equals(entry.getTriggeringCardId()) ? snapshot
                    : exiled != null && !exiled.faceDown() ? exiled.card() : null;
            eventCards = known == null ? List.of() : List.of(known);
        }
        List<LocatedCard> originals = new ArrayList<>();
        Set<UUID> seen = new LinkedHashSet<>();
        for (Card card : eventCards) {
            if (!seen.add(card.getId()) || card.isToken()) {
                continue;
            }
            LocatedCard current = named.get(card.getId());
            if (current != null) {
                originals.add(current);
                continue;
            }
            if (locate(gameData, card.getId(), Zone.GRAVEYARD) != null
                    || triggering.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                    card, triggering.filter(), entry.getCard().getId(), gameData, gameData.ownerOf(card))) {
                // Still in its graveyard but in a later stay is a new object (CR 400.7); or it doesn't match.
                continue;
            }
            originals.add(new LocatedCard(card, Zone.GRAVEYARD, gameData.ownerOf(card)));
        }
        if (originals.isEmpty() && eventCards.isEmpty()) {
            return List.copyOf(named.values());
        }
        return originals;
    }

    /**
     * {@code cardsThisWay(CHOOSE)} for a duplicate or a copy: each chosen card where it is, and a chosen card of the
     * trigger event that has left its zone ({@link CardSubject.TriggeringCards#asLastKnown()}: "a card from among those
     * cards" that left a graveyard) as the event recorded it, its last known information (CR 608.2h).
     */
    private List<LocatedCard> chosenCardsAsLastKnown(GameData gameData, StackEntry entry, CardEffect effect,
                                                     CardSubject.CardsThisWay chosen) {
        List<LocatedCard> located = new ArrayList<>(locatedCards(gameData, entry, effect, chosen).stream()
                .filter(card -> !card.card().isToken()).toList());
        Set<UUID> found = new LinkedHashSet<>();
        located.forEach(card -> found.add(card.card().getId()));
        List<UUID> chosenIds = entry.getResolutionResults().outcome(StepKind.CHOOSE).objectIds();
        if (found.containsAll(chosenIds) || entry.getTriggeringCards().isEmpty()) {
            return located;
        }
        Map<UUID, LocatedCard> eventCards = new LinkedHashMap<>();
        triggeringCardsAsLastKnown(gameData, entry, new CardSubject.TriggeringCards(null, true))
                .forEach(card -> eventCards.putIfAbsent(card.card().getId(), card));
        for (UUID id : chosenIds) {
            LocatedCard lastKnown = eventCards.get(id);
            if (!found.contains(id) && lastKnown != null && (chosen.filter() == null || cardMatches(gameData, entry,
                    lastKnown.card(), lastKnown.ownerId(), chosen.filter()))) {
                located.add(lastKnown);
            }
        }
        return located;
    }

    /** The cards a "put into a hand" or seek event was about, as the entry recorded them, each once. */
    private static List<Card> eventCardsPutIntoHand(StackEntry entry) {
        if (!entry.getTriggeringCards().isEmpty()) {
            Map<UUID, Card> byId = new LinkedHashMap<>();
            entry.getTriggeringCards().forEach(card -> byId.putIfAbsent(card.getId(), card));
            return List.copyOf(byId.values());
        }
        return entry.getTriggeringCardSnapshot() == null ? List.of() : List.of(entry.getTriggeringCardSnapshot());
    }

    /** A card a {@link CardSubject} names, in the zone it is in ({@link Zone#GRAVEYARD}, {@link Zone#EXILE},
     * {@link Zone#COMMAND}, or a hand or library for a subject of one, a sought, revealed or chosen card; the
     * battlefield or the stack only for {@link CardSubject.Owned}). */
    public record LocatedCard(Card card, Zone zone, UUID ownerId) {
    }

    /**
     * {@link CardSubject.CardsThisWay}: for each object the step recorded, the card it became in the zone the step put
     * it in ({@link StepOutcome#movedCards()}; every card of a merged permanent, CR 731.3c); an exiled card that kept
     * its id is looked for in exile, a discarded card in its graveyard, else in exile where a replacement put it, a revealed card in the library it
     * was revealed from, a card a library search found in the zone the search put it in (a graveyard card a replacement
     * exiled instead, in exile). Only
     * a card still in that zone is named, and, for a delayed ability's step, in the stay it was bound to
     * ({@link MovedCard#entryVersion()}, CR 603.7c); the subject's filter, when it has one, is then judged on each card
     * as it is there.
     */
    private List<LocatedCard> cardsThisWay(GameData gameData, StackEntry entry, StepKind kind) {
        StepOutcome outcome = entry.getResolutionResults().outcome(kind);
        Map<UUID, List<MovedCard>> moved = new LinkedHashMap<>();
        outcome.movedCards().forEach(card ->
                moved.computeIfAbsent(card.objectId(), id -> new ArrayList<>()).add(card));
        List<LocatedCard> located = new ArrayList<>();
        Set<UUID> seen = new LinkedHashSet<>();
        for (UUID objectId : outcome.objectIds()) {
            List<MovedCard> cards = moved.getOrDefault(objectId, kind == StepKind.EXILE
                    ? List.of(new MovedCard(objectId, objectId, Zone.EXILE, null)) : List.of());
            if (cards.isEmpty() && (kind == StepKind.DISCARD || kind == StepKind.MILL)) {
                LocatedCard discarded = locate(gameData, objectId, Zone.GRAVEYARD);
                if (discarded == null) {
                    discarded = locate(gameData, objectId, Zone.EXILE);
                }
                if (discarded != null && seen.add(objectId)) {
                    located.add(discarded);
                }
                continue;
            }
            for (MovedCard card : cards) {
                if (!seen.add(card.cardId()) || !gameData.isInStay(card)) {
                    continue;
                }
                LocatedCard found = locate(gameData, card.cardId(), card.zone());
                if (found == null && kind == StepKind.SEARCH && card.zone() == Zone.GRAVEYARD) {
                    // A card a search put into a graveyard that a replacement exiled instead (Rest in Peace).
                    found = locate(gameData, card.cardId(), Zone.EXILE);
                }
                // A destroyed or sacrificed token is no card, in the moment before it ceases to exist (CR 108.2b).
                if (found != null && !((kind == StepKind.DESTROY || kind == StepKind.SACRIFICE)
                        && found.card().isToken())) {
                    located.add(found);
                }
            }
        }
        return located;
    }

    /**
     * {@link CardSubject.TriggeringCards}: the event's cards in the graveyard stay it put them in, each card an event
     * exiled (every card of a "one or more" event) in that exile stay, face up or face down (CR 400.2, 400.7e). The
     * filter sees a card exiled face down as a card with no characteristics (CR 406.3a).
     */
    private List<LocatedCard> triggeringCards(GameData gameData, StackEntry entry,
                                              CardSubject.TriggeringCards triggering) {
        List<UUID> ids = entry.triggeringCardIds();
        // An ability of the exiled object's own ("when this is put into exile") is about its card.
        UUID eventCardId = entry.getTriggeringCardId() == null && entry.getTriggeringCardExileEntryVersion() >= 0
                ? entry.getCard().getId() : entry.getTriggeringCardId();
        if (ids.isEmpty() && eventCardId != null) {
            ids = List.of(eventCardId);
        }
        List<LocatedCard> named = new ArrayList<>();
        for (UUID id : new LinkedHashSet<>(ids)) {
            LocatedCard card;
            boolean faceDown = false;
            Long exileStay = entry.getEventCardExileEntryVersions().get(id);
            if (exileStay == null && id.equals(eventCardId) && entry.getTriggeringCardExileEntryVersion() >= 0) {
                exileStay = entry.getTriggeringCardExileEntryVersion();
            }
            if (exileStay != null) {
                ExiledCardEntry exiled = gameData.findExiledCard(id);
                card = exiled == null || gameData.exileEntryVersions.getOrDefault(id, 0L) != exileStay
                        ? null : new LocatedCard(exiled.card(), Zone.EXILE, exiled.ownerId());
                faceDown = exiled != null && exiled.faceDown();
            } else {
                long stay = gameData.graveyardEntryVersion(id);
                Long eventStay = entry.getEventCardGraveyardEntryVersions().get(id);
                if (eventStay != null ? eventStay != stay
                        : id.equals(entry.getTriggeringCardId()) && !entry.isTriggeringCardIncarnation(stay)) {
                    continue;
                }
                card = locate(gameData, id, Zone.GRAVEYARD);
            }
            if (card == null || card.card().isToken()) {
                continue;
            }
            if (triggering.filter() != null) {
                // A card of its own, so neither the hidden face nor what is remembered by its id is read.
                Card judged = faceDown ? Card.namedRuntimePlaceholder("") : card.card();
                if (!predicateEvaluationService.matchesCardPredicate(
                        judged, triggering.filter(), entry.getCard().getId(), gameData, card.ownerId())) {
                    continue;
                }
            }
            named.add(card);
        }
        return named;
    }

    /** {@link CardSubject.InGraveyards}: no token, which is no card (CR 108.2b). */
    private List<LocatedCard> inGraveyards(GameData gameData, StackEntry entry, CardEffect effect,
                                           CardSubject.InGraveyards in) {
        List<LocatedCard> named = new ArrayList<>();
        for (UUID ownerId : players(gameData, entry, effect, in.owners())) {
            for (Card card : List.copyOf(gameData.playerGraveyards.getOrDefault(ownerId, List.of()))) {
                if (!card.isToken() && cardMatches(gameData, entry, card, ownerId, in.filter())) {
                    named.add(new LocatedCard(card, Zone.GRAVEYARD, ownerId));
                }
            }
        }
        return named;
    }

    /**
     * {@link CardSubject.OutsideTheGame}: each owner's cards outside the game, judged with no game context, as no effect
     * of the game applies to them (CR 400.11).
     */
    private List<LocatedCard> outsideTheGame(GameData gameData, StackEntry entry, CardEffect effect,
                                             CardSubject.OutsideTheGame outside) {
        List<LocatedCard> named = new ArrayList<>();
        for (UUID ownerId : players(gameData, entry, effect, outside.owners())) {
            for (Card card : List.copyOf(OutsideGameCards.view(gameData, ownerId))) {
                if (outside.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, outside.filter(), null, null, ownerId)) {
                    named.add(new LocatedCard(card, Zone.OUTSIDE_GAME, ownerId));
                }
            }
        }
        return named;
    }

    /** {@link CardSubject.InHand}, {@link CardSubject.InLibrary}, {@link CardSubject.InCommandZone}. */
    private List<LocatedCard> inZone(GameData gameData, StackEntry entry, CardEffect effect, PlayerSubject owners,
                                     CardPredicate filter, Zone zone, Map<UUID, List<Card>> zones) {
        List<LocatedCard> named = new ArrayList<>();
        for (UUID ownerId : players(gameData, entry, effect, owners)) {
            for (Card card : List.copyOf(zones.getOrDefault(ownerId, List.of()))) {
                if (!card.isToken() && cardMatches(gameData, entry, card, ownerId, filter)) {
                    named.add(new LocatedCard(card, zone, ownerId));
                }
            }
        }
        return named;
    }

    /**
     * {@link CardSubject.Owned}: the matching cards those players own in each zone, each once: a library, hand,
     * graveyard or command zone card (one with no owner recorded belongs to the player whose zone it is in), an exiled
     * card, a nontoken permanent's physical card judged as the permanent is now, and a spell's card, no copy.
     */
    private List<LocatedCard> owned(GameData gameData, StackEntry entry, CardEffect effect, CardSubject.Owned owned) {
        Set<UUID> owners = new LinkedHashSet<>(players(gameData, entry, effect, owned.owners()));
        Map<UUID, LocatedCard> named = new LinkedHashMap<>();
        for (var zone : List.of(Map.entry(Zone.LIBRARY, gameData.playerDecks), Map.entry(Zone.HAND, gameData.playerHands),
                Map.entry(Zone.GRAVEYARD, gameData.playerGraveyards),
                Map.entry(Zone.COMMAND, gameData.playerCommandZones))) {
            zone.getValue().forEach((zoneOwnerId, cards) -> {
                for (Card card : List.copyOf(cards)) {
                    UUID ownerId = Objects.requireNonNullElse(gameData.ownerOf(card), zoneOwnerId);
                    addOwned(gameData, entry, owned, owners, named, card, card, zone.getKey(), ownerId);
                }
            });
        }
        gameData.playerBattlefields.forEach((controllerId, permanents) -> {
            for (Permanent permanent : List.copyOf(permanents)) {
                Card physical = permanent.getOriginalCard() != null ? permanent.getOriginalCard() : permanent.getCard();
                if (permanent.getCard().isToken() || physical.isToken()) {
                    continue;
                }
                UUID ownerId = Objects.requireNonNullElse(gameData.ownerOf(physical), controllerId);
                addOwned(gameData, entry, owned, owners, named, permanent.getCard(), physical, Zone.BATTLEFIELD,
                        ownerId);
            }
        });
        List<ExiledCardEntry> exiled;
        synchronized (gameData.exiledCards) {
            exiled = List.copyOf(gameData.exiledCards);
        }
        for (ExiledCardEntry held : exiled) {
            addOwned(gameData, entry, owned, owners, named, held.card(), held.card(), Zone.EXILE, held.ownerId());
        }
        for (StackEntry spell : List.copyOf(gameData.stack)) {
            if (spell.getEntryType().isAbility() || spell.isCopy() || spell.getCard() == null) {
                continue;
            }
            addOwned(gameData, entry, owned, owners, named, spell.getCard(), spell.getCard(), Zone.STACK,
                    gameData.ownerOf(spell.getCard()));
        }
        return List.copyOf(named.values());
    }

    /** Names {@code physical} for {@link #owned} when it is a card one of {@code owners} owns and {@code judged} matches. */
    private void addOwned(GameData gameData, StackEntry entry, CardSubject.Owned owned, Set<UUID> owners,
                          Map<UUID, LocatedCard> named, Card judged, Card physical, Zone zone, UUID ownerId) {
        if (physical.isToken() || ownerId == null || !owners.contains(ownerId) || named.containsKey(physical.getId())
                || !cardMatches(gameData, entry, judged, ownerId, owned.filter())) {
            return;
        }
        named.put(physical.getId(), new LocatedCard(physical, zone, ownerId));
    }

    /** {@link CardSubject.TopmostInLibrary}: in each library, the first card from the top that matches. */
    private List<LocatedCard> topmostInLibrary(GameData gameData, StackEntry entry, CardEffect effect,
                                               CardSubject.TopmostInLibrary top) {
        List<LocatedCard> named = new ArrayList<>();
        for (UUID ownerId : players(gameData, entry, effect, top.owners())) {
            for (Card card : List.copyOf(gameData.playerDecks.getOrDefault(ownerId, List.of()))) {
                if (!card.isToken() && cardMatches(gameData, entry, card, ownerId, top.filter())) {
                    named.add(new LocatedCard(card, Zone.LIBRARY, ownerId));
                    break;
                }
            }
        }
        return named;
    }

    /** {@link CardSubject.TopOfGraveyard}: the last card of each graveyard that is a card (CR 108.2b). */
    private List<LocatedCard> topOfGraveyard(GameData gameData, StackEntry entry, CardEffect effect,
                                             CardSubject.TopOfGraveyard top) {
        List<LocatedCard> named = new ArrayList<>();
        for (UUID ownerId : players(gameData, entry, effect, top.owners())) {
            List<Card> graveyard = List.copyOf(gameData.playerGraveyards.getOrDefault(ownerId, List.of()));
            for (Card card : graveyard.reversed()) {
                if (card.isToken()) {
                    continue;
                }
                if (cardMatches(gameData, entry, card, ownerId, top.filter())) {
                    named.add(new LocatedCard(card, Zone.GRAVEYARD, ownerId));
                }
                break;
            }
        }
        return named;
    }

    /**
     * {@link CardSubject.TargetExiledCard}: the effect's targeted card while it is face up in exile, with the link and
     * the owner it was targeted for and matching the filter as it did then (CR 608.2b).
     */
    private List<LocatedCard> targetExiledCard(GameData gameData, StackEntry entry, CardEffect effect,
                                               CardSubject.TargetExiledCard target) {
        UUID cardId = entry.targetsForEffect(effect).stream()
                .filter(id -> gameData.findExiledCard(id) != null).findFirst().orElse(entry.getTargetId());
        ExiledCardEntry exiled = cardId == null ? null : gameData.findExiledCard(cardId);
        if (exiled == null || exiled.faceDown() || gameData.antedCardIds.contains(cardId)) {
            return List.of();
        }
        if (target.exiledWithSource() && !linkedSourceId(entry).equals(exiled.sourcePermanentId())) {
            return List.of();
        }
        boolean ownCard = entry.getControllerId().equals(exiled.ownerId());
        if (target.owner() == com.github.laxika.magicalvibes.model.filter.PlayerRelation.SELF && !ownCard
                || target.owner() == com.github.laxika.magicalvibes.model.filter.PlayerRelation.OPPONENT && ownCard) {
            return List.of();
        }
        return cardMatches(gameData, entry, exiled.card(), exiled.ownerId(), target.filter())
                ? List.of(new LocatedCard(exiled.card(), Zone.EXILE, exiled.ownerId())) : List.of();
    }

    /** The permanent the source is or last was, as the entry or its snapshot has it, else the source card. */
    private static UUID linkedSourceId(StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        return entry.getSourcePermanentSnapshot() != null
                ? entry.getSourcePermanentSnapshot().getId() : entry.getCard().getId();
    }

    /** {@link CardSubject.InExile}. */
    private List<LocatedCard> inExile(GameData gameData, StackEntry entry, CardEffect effect, CardSubject.InExile in) {
        List<UUID> owners = in.owners() == null ? null : players(gameData, entry, effect, in.owners());
        List<ExiledCardEntry> inZone;
        synchronized (gameData.exiledCards) {
            inZone = gameData.exiledCards.stream()
                    .filter(exiled -> owners == null || owners.contains(exiled.ownerId())).toList();
        }
        return exiledMatching(gameData, entry, inZone, in.filter());
    }

    /**
     * {@link CardSubject.ExiledWithSource}: linked to the source permanent, as the entry or its last-known snapshot
     * has it, else to the source card.
     */
    private List<LocatedCard> exiledWithSource(GameData gameData, StackEntry entry, CardEffect effect,
                                               CardSubject.ExiledWithSource with) {
        UUID sourceId = linkedSourceId(entry);
        List<UUID> owners = with.owners() == null ? null : players(gameData, entry, effect, with.owners());
        List<ExiledCardEntry> inZone;
        synchronized (gameData.exiledCards) {
            inZone = gameData.exiledCards.stream()
                    .filter(exiled -> exiledWithSourceBy(exiled, sourceId, with.abilityLink()))
                    .filter(exiled -> owners == null || owners.contains(exiled.ownerId())).toList();
        }
        return exiledMatching(gameData, entry, inZone, with.filter());
    }

    /**
     * {@link CardSubject.ExiledAsCost}: the card a cast or activation cost exiled, and the cards an activated ability's
     * tracked graveyard-exile cost exiled ("exile two creature cards from your graveyard: ... one of the exiled
     * cards"), those still in exile.
     */
    private List<LocatedCard> exiledAsCost(GameData gameData, StackEntry entry) {
        Set<UUID> cardIds = new LinkedHashSet<>();
        if (entry.getExiledCostCardId() != null) {
            cardIds.add(entry.getExiledCostCardId());
        }
        cardIds.addAll(entry.getActivatedAbilityExiledCardIds());
        List<ExiledCardEntry> held = cardIds.stream()
                .map(gameData::findExiledCard)
                .filter(Objects::nonNull)
                .toList();
        return exiledMatching(gameData, entry, held, null);
    }

    private List<LocatedCard> exiledMatching(GameData gameData, StackEntry entry, List<ExiledCardEntry> exiled,
                                             CardPredicate filter) {
        List<LocatedCard> named = new ArrayList<>();
        for (ExiledCardEntry held : exiled) {
            // A card exiled face down has no characteristics (CR 406.3a).
            Card judged = held.faceDown() ? Card.namedRuntimePlaceholder("") : held.card();
            if (!held.card().isToken() && cardMatches(gameData, entry, judged, held.ownerId(), filter)) {
                named.add(new LocatedCard(held.card(), Zone.EXILE, held.ownerId()));
            }
        }
        return named;
    }

    private boolean cardMatches(GameData gameData, StackEntry entry, Card card, UUID ownerId, CardPredicate filter) {
        return filter == null || predicateEvaluationService.matchesCardPredicate(card,
                AmountEvaluationService.bindTriggeringSpellManaValue(gameData, filter, entry), entry.getCard().getId(),
                gameData, ownerId, null, null, entry.getXValue(), entry.getSourcePermanentSnapshot());
    }

    /**
     * {@link CardSubject.SourceCard}: the ability's card in the exile or graveyard stay it was put on the stack from; an
     * ability of a card in exile names it in exile, face down too, as its own card (CR 400.7).
     */
    private List<LocatedCard> sourceCard(GameData gameData, StackEntry entry) {
        UUID cardId = entry.getCard().getId();
        long exileStay = entry.getSourceExileEntryVersion();
        if (exileStay >= 0) {
            LocatedCard exiled = gameData.exileEntryVersionsOf(List.of(cardId)).get(cardId) != exileStay
                    ? null : locate(gameData, cardId, Zone.EXILE);
            return exiled == null ? List.of() : List.of(exiled);
        }
        long stay = entry.sourceCardGraveyardEntryVersion();
        LocatedCard card = stay >= 0 && gameData.graveyardEntryVersion(cardId) != stay
                ? null : locate(gameData, cardId, Zone.GRAVEYARD);
        return card == null ? List.of() : List.of(card);
    }

    /**
     * {@link CardSubject.TargetCard}: the effect's targeted card while it is in a graveyard of the subject's scope and
     * matches its filter, judged as the target was chosen (CR 608.2b).
     */
    private List<LocatedCard> targetCard(GameData gameData, StackEntry entry, CardSubject.TargetCard target) {
        UUID cardId = entry.getTargetId();
        if ((cardId == null || gameQueryService.findCardInGraveyardById(gameData, cardId) == null)
                && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            cardId = entry.getTargetCardIds().getFirst();
        }
        LocatedCard found = cardId == null ? null : locate(gameData, cardId, Zone.GRAVEYARD);
        if (found == null || !target.scope().graveyardOwners(gameData.orderedPlayerIds, entry.getControllerId())
                .contains(found.ownerId())) {
            return List.of();
        }
        if (target.filter() != null && !predicateEvaluationService.matchesCardPredicate(found.card(), target.filter(),
                entry.getCard().getId(), gameData, found.ownerId(), null, null, entry.getXValue(),
                entry.getSourcePermanentSnapshot())) {
            return List.of();
        }
        return List.of(found);
    }

    private LocatedCard locate(GameData gameData, UUID cardId, Zone zone) {
        if (zone == Zone.GRAVEYARD) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            return card == null ? null
                    : new LocatedCard(card, Zone.GRAVEYARD, gameQueryService.findGraveyardOwnerById(gameData, cardId));
        }
        if (zone == Zone.EXILE) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            return exiled == null ? null : new LocatedCard(exiled.card(), Zone.EXILE, exiled.ownerId());
        }
        if (zone == Zone.OUTSIDE_GAME) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Card card : OutsideGameCards.view(gameData, playerId)) {
                    if (card.getId().equals(cardId)) {
                        return new LocatedCard(card, zone, playerId);
                    }
                }
            }
            return null;
        }
        if (zone == Zone.HAND || zone == Zone.LIBRARY || zone == Zone.COMMAND) {
            Map<UUID, List<Card>> zones = zone == Zone.HAND ? gameData.playerHands
                    : zone == Zone.LIBRARY ? gameData.playerDecks : gameData.playerCommandZones;
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Card card : zones.getOrDefault(playerId, List.of())) {
                    if (card.getId().equals(cardId)) {
                        return new LocatedCard(card, zone, playerId);
                    }
                }
            }
        }
        return null;
    }

    /**
     * The rule a resolving effect that changes no characteristics makes of a set ("creatures target player controls
     * can't block this turn"): it is not locked to the permanents found now but covers whatever matches whenever it is
     * checked, those arriving later included (CR 611.2c). What the set's words fix as the effect resolves is bound now:
     * its players ({@code eachPlayer()} binds none), a {@link PermanentIsTargetCardPredicate} leaf becomes the
     * effect's target permanent ("other creatures that player controls"), and the values this resolution chose
     * ({@link #bindChosenValues(GameData, StackEntry, CardEffect, PermanentPredicate)}: "creatures with mana value of
     * that quality", Twins of Discord). The returned predicate is judged later without a stack entry. {@code null} when
     * the set names no player.
     */
    public PermanentPredicate lastingRule(GameData gameData, StackEntry entry, CardEffect effect,
                                          PermanentSubject.ControlledBy set) {
        Context ctx = new Context(gameData, entry, effect, null, entry.getControllerId());
        PermanentPredicate filter = bindChosenValues(gameData, entry, effect, bindTarget(ctx, set.filter()));
        if (set.players() instanceof PlayerSubject.Each each && each.relation() == PlayerSubject.Relation.ALL) {
            return filter;
        }
        List<UUID> players = players(ctx, set.players());
        if (players.isEmpty()) {
            return null;
        }
        PermanentPredicate controlled = players.size() == 1
                ? new PermanentControlledByPlayerPredicate(players.getFirst())
                : new PermanentAnyOfPredicate(players.stream()
                        .<PermanentPredicate>map(PermanentControlledByPlayerPredicate::new)
                        .toList());
        return filter instanceof PermanentTruePredicate ? controlled
                : new PermanentAllOfPredicate(List.of(controlled, filter));
    }

    /**
     * {@link #lastingRule(GameData, StackEntry, CardEffect, PermanentSubject.ControlledBy)} of a set but the
     * permanents another subject names ("other creatures they control" after a choice): those are found now, as the
     * effect resolves, and stay left out whatever they become. {@code null} when the set names no player.
     */
    public PermanentPredicate lastingRule(GameData gameData, StackEntry entry, CardEffect effect,
                                          PermanentSubject.Except except) {
        if (!(except.permanents() instanceof PermanentSubject.ControlledBy set)) {
            throw new IllegalArgumentException("A lasting rule is made of a set: " + except.permanents());
        }
        PermanentPredicate rule = lastingRule(gameData, entry, effect, set);
        if (rule == null) {
            return null;
        }
        List<PermanentPredicate> excluded = ids(new Context(gameData, entry, effect, null, entry.getControllerId()),
                except.excluded()).stream()
                .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new)
                .toList();
        return excluded.isEmpty() ? rule : new PermanentAllOfPredicate(List.of(rule,
                new PermanentNotPredicate(excluded.size() == 1 ? excluded.getFirst()
                        : new PermanentAnyOfPredicate(excluded))));
    }

    private PermanentPredicate bindTarget(Context ctx, PermanentPredicate predicate) {
        return switch (predicate) {
            case PermanentIsTargetCardPredicate ignored -> {
                List<PermanentPredicate> targets = ids(ctx, Subjects.target()).stream()
                        .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new)
                        .toList();
                yield targets.size() == 1 ? targets.getFirst() : new PermanentAnyOfPredicate(targets);
            }
            case PermanentPowerLessThanTargetPredicate ignored -> lessThanTargetsPower(ctx);
            case PermanentAllOfPredicate allOf -> new PermanentAllOfPredicate(allOf.predicates().stream()
                    .map(part -> bindTarget(ctx, part))
                    .toList());
            case PermanentAnyOfPredicate anyOf -> new PermanentAnyOfPredicate(anyOf.predicates().stream()
                    .map(part -> bindTarget(ctx, part))
                    .toList());
            case PermanentNotPredicate not -> new PermanentNotPredicate(bindTarget(ctx, not.predicate()));
            default -> predicate;
        };
    }

    /** The target's power as it is, else as it last was (CR 608.2h); with no target, no permanent has less. */
    private PermanentPredicate lessThanTargetsPower(Context ctx) {
        for (UUID targetId : ids(ctx, Subjects.target())) {
            Permanent target = gameQueryService.findPermanentById(ctx.gameData(), targetId);
            Integer power = target != null ? gameQueryService.getEffectivePower(ctx.gameData(), target)
                    : ctx.entry().getLastKnownPermanentPowers().get(targetId);
            if (power != null) {
                return new PermanentPowerAtMostPredicate(power - 1);
            }
        }
        return new PermanentAnyOfPredicate(List.of());
    }

    /** The permanents {@code subject} names for a static ability of {@code source}. */
    public List<Permanent> permanents(GameData gameData, Permanent source, PermanentSubject subject) {
        requireStatic(subject.needsStackEntry(), subject);
        return livePermanents(new Context(gameData, null, null, source,
                gameQueryService.findPermanentController(gameData, source.getId())), subject);
    }

    /**
     * The permanents {@code subject} names for a static ability of {@code source} whose controller the caller knows
     * because {@code source} is not on the battlefield (an emblem's, given its source card as a stand-in permanent).
     */
    public List<Permanent> permanents(GameData gameData, Permanent source, UUID controllerId,
                                      PermanentSubject subject) {
        requireStatic(subject.needsStackEntry(), subject);
        return livePermanents(new Context(gameData, null, null, source, controllerId), subject);
    }

    /** The players {@code subject} names, in APNAP order, for a static ability of {@code source}. */
    public List<UUID> players(GameData gameData, Permanent source, PlayerSubject subject) {
        requireStatic(subject.needsStackEntry(), subject);
        return players(new Context(gameData, null, null, source,
                gameQueryService.findPermanentController(gameData, source.getId())), subject);
    }

    /**
     * Whether {@code subject}, for a static ability of the context's source, names the context's target and the target
     * matches {@code filter}: the static entry point asked about one permanent, which is how the layered pass applies a
     * static ability (it re-evaluates the set on every pass, CR 611.3a). It judges the subject's structure itself (the
     * source, its attachment, whose battlefield the target is on) and leaves every characteristic to {@code reads},
     * which answers from the layered pass in progress, so a pass never re-enters itself. It looks nothing up and
     * allocates nothing.
     *
     * @throws IllegalArgumentException for a subject that needs a stack entry
     * @throws UnsupportedOperationException for a subject with no one-permanent form yet ({@code ownedBy},
     *                                       {@code attachmentsOf})
     */
    public boolean names(StaticEffectContext context, PermanentSubject subject, PermanentPredicate filter,
                         StaticReads reads) {
        requireStatic(subject.needsStackEntry(), subject);
        return namesUnfiltered(context.gameData(), context.source(), context.sourceId(), context.sourceControllerId(),
                context.targetOnSameBattlefield(), null, context.target(), subject, reads)
                && (filter == null || reads.matchesFilter(context, context.target(), filter));
    }

    /**
     * {@link #names(StaticEffectContext, PermanentSubject, PermanentPredicate, StaticReads)} with no filter, for a
     * scan that has no {@link StaticEffectContext} at hand: {@code target} is on the battlefield of
     * {@code sourceControllerId} exactly when {@code targetOnSourcesBattlefield}.
     */
    public boolean names(GameData gameData, Permanent source, UUID sourceControllerId,
                         boolean targetOnSourcesBattlefield, Permanent target, PermanentSubject subject,
                         StaticReads reads) {
        requireStatic(subject.needsStackEntry(), subject);
        return namesUnfiltered(gameData, source, source.getId(), sourceControllerId, targetOnSourcesBattlefield,
                null, target, subject, reads);
    }

    /**
     * {@link #names(GameData, Permanent, UUID, boolean, Permanent, PermanentSubject, StaticReads)} for a scan that knows
     * whose battlefield {@code target} is on, or will enter ({@code targetControllerId}), which also judges an
     * enchanted player's set without searching the battlefields. {@code source} may be {@code null} for a static
     * ability with no source permanent (an emblem's, a plane's): {@code source()} and {@code attached()} then name
     * nothing, and {@code sourceControllerId} is the ability's controller.
     */
    public boolean names(GameData gameData, Permanent source, UUID sourceControllerId, UUID targetControllerId,
                         Permanent target, PermanentSubject subject, StaticReads reads) {
        requireStatic(subject.needsStackEntry(), subject);
        return namesUnfiltered(gameData, source, source == null ? null : source.getId(), sourceControllerId,
                targetControllerId != null && targetControllerId.equals(sourceControllerId), targetControllerId,
                target, subject, reads);
    }

    /**
     * Whether {@code players}, for a static ability, names {@code playerId}: the one-player form of the static entry
     * point, for a query that already knows the ability's controller. It judges {@code you()}, {@code eachPlayer()},
     * {@code eachOpponent()}, {@code enchantedPlayer()} and {@code chosenPlayer()} without looking anything up;
     * {@code source} may be {@code null} for an ability with no source permanent (an emblem's, a plane's), which then
     * enchants and chose nobody. With the board it also judges {@code controllerOf(attached())}, "enchanted
     * creature's controller" (Brand of Ill Omen): the player whose battlefield holds the permanent the source is
     * attached to, nobody while it is attached to nothing or to a player; {@code ownerOf(source())}, "its owner"
     * (Xantcha): the owner of the source's card, an in-game ownership change included; {@code activePlayer()}, the
     * player whose turn it is; and {@code except(players, excluded)} over any of these forms ("except during its
     * controller's turn", Defense Grid).
     *
     * @throws UnsupportedOperationException for any other subject
     */
    public static boolean namesPlayer(GameData gameData, Permanent source, UUID controllerId, UUID playerId,
                                      PlayerSubject players) {
        if (players instanceof PlayerSubject.ActivePlayer) {
            return playerId != null && playerId.equals(gameData.activePlayerId);
        }
        if (players instanceof PlayerSubject.Except(PlayerSubject included, PlayerSubject excluded)) {
            return namesPlayer(gameData, source, controllerId, playerId, included)
                    && !namesPlayer(gameData, source, controllerId, playerId, excluded);
        }
        if (players instanceof PlayerSubject.ControllerOf(PermanentSubject.Attached(boolean creature, boolean last))
                && !creature && !last) {
            if (playerId == null || source == null || source.getAttachedTo() == null) {
                return false;
            }
            for (UUID battlefieldOwner : gameData.orderedPlayerIds) {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(battlefieldOwner, List.of())) {
                    if (permanent.getId().equals(source.getAttachedTo())) {
                        return playerId.equals(battlefieldOwner);
                    }
                }
            }
            return false;
        }
        if (players instanceof PlayerSubject.OwnerOf(PermanentSubject.Source ignored)) {
            return playerId != null && source != null && playerId.equals(ownerOfLive(gameData, source));
        }
        return namesPlayer(source, controllerId, playerId, players);
    }

    /**
     * As {@link #namesPlayer(GameData, Permanent, UUID, UUID, PlayerSubject)} without the forms that look the
     * board up, {@code controllerOf(attached())} and {@code ownerOf(source())}; {@code controllerOf(source())} is the
     * ability's controller.
     *
     * @throws UnsupportedOperationException for any other subject
     */
    public static boolean namesPlayer(Permanent source, UUID controllerId, UUID playerId, PlayerSubject players) {
        return switch (players) {
            case PlayerSubject.You ignored -> playerId != null && playerId.equals(controllerId);
            case PlayerSubject.ControllerOf(PermanentSubject.Source ignored) ->
                    playerId != null && playerId.equals(controllerId);
            case PlayerSubject.Each each -> each.relation() == PlayerSubject.Relation.ALL
                    || playerId != null && !playerId.equals(controllerId);
            case PlayerSubject.EnchantedPlayer ignored -> playerId != null && source != null
                    && playerId.equals(source.getAttachedTo());
            case PlayerSubject.ChosenPlayer ignored -> playerId != null && source != null
                    && playerId.equals(source.getRememberedTargetPlayerId());
            default -> throw new UnsupportedOperationException("No one-player static form for " + players + " yet");
        };
    }

    /**
     * Whether {@code cards}, for a static ability of {@code source} controlled by {@code controllerId}, names the
     * exiled card {@code exiled}: the one-card form of the static entry point, asked about one card ("you may cast
     * spells from among cards in exile your opponents own with ice counters on them"). Only
     * {@code exiledCards(players, filter)} and the cards exiled with {@code source} ({@code exiledWithSource}, by one
     * linked ability when it names one) have one: their players are judged by
     * {@link #namesPlayer(Permanent, UUID, UUID, PlayerSubject)}, their filters by {@code reads}, given the card as the
     * filter may see it (a card exiled face down has no characteristics, CR 406.3a) and its owner. A token in exile
     * is never named.
     *
     * @throws UnsupportedOperationException for any other subject
     */
    public static boolean namesExiledCard(Permanent source, UUID controllerId, ExiledCardEntry exiled,
                                          CardSubject cards, ExiledCardReads reads) {
        PlayerSubject owners;
        CardPredicate filter;
        switch (cards) {
            case CardSubject.InExile in -> {
                owners = in.owners();
                filter = in.filter();
            }
            case CardSubject.ExiledWithSource with -> {
                if (source == null || !exiledWithSourceBy(exiled, source.getId(), with.abilityLink())) {
                    return false;
                }
                owners = with.owners();
                filter = with.filter();
            }
            default -> throw new UnsupportedOperationException("No one-card static form for " + cards + " yet");
        }
        if (exiled.card().isToken()
                || owners != null && !namesPlayer(source, controllerId, exiled.ownerId(), owners)) {
            return false;
        }
        Card judged = exiled.faceDown() ? Card.namedRuntimePlaceholder("") : exiled.card();
        return filter == null || reads.matches(judged, exiled.ownerId(), filter);
    }

    /**
     * The cards {@code cards} names for a static ability of {@code source} controlled by {@code controllerId}, as they
     * are now: the list form of the static entry point, for a static ability that reads cards outside the battlefield
     * ("has all activated abilities of all creature cards in all graveyards", "as long as a card exiled with it has
     * flying"). It looks up only the zones and the source; players are judged by
     * {@link #namesPlayer(Permanent, UUID, UUID, PlayerSubject)} and filters by {@code reads}, so a layered pass can call
     * it. The static forms:
     * <ul>
     *   <li>{@link CardSubject.InExile} and {@link CardSubject.ExiledWithSource}: each exiled card
     *       {@link #namesExiledCard} names (a card exiled face down is judged as one with no characteristics,
     *       CR 406.3a);</li>
     *   <li>{@link CardSubject.InGraveyards}: the matching cards in the graveyards of the players it names, in turn
     *       order, no token (CR 108.2b);</li>
     *   <li>{@link CardSubject.TopmostInLibrary}: in each named library the first card from the top that matches;</li>
     *   <li>{@link CardSubject.ExiledAsCost}: the cards exiled to pay the cost of the spell {@code source} was with
     *       delve (CR 702.66a), wherever they are now, located as exiled.</li>
     * </ul>
     *
     * @throws UnsupportedOperationException for any other subject
     */
    public static List<LocatedCard> namedCards(GameData gameData, Permanent source, UUID controllerId,
                                               CardSubject cards, ExiledCardReads reads) {
        List<LocatedCard> named = new ArrayList<>();
        switch (cards) {
            case CardSubject.InExile ignored -> namedExiledCards(gameData, source, controllerId, cards, reads, named);
            case CardSubject.ExiledWithSource ignored ->
                    namedExiledCards(gameData, source, controllerId, cards, reads, named);
            case CardSubject.InGraveyards in -> {
                for (UUID ownerId : gameData.orderedPlayerIds) {
                    if (!namesPlayer(source, controllerId, ownerId, in.owners())) {
                        continue;
                    }
                    for (Card card : List.copyOf(gameData.playerGraveyards.getOrDefault(ownerId, List.of()))) {
                        if (!card.isToken() && (in.filter() == null || reads.matches(card, ownerId, in.filter()))) {
                            named.add(new LocatedCard(card, Zone.GRAVEYARD, ownerId));
                        }
                    }
                }
            }
            case CardSubject.TopmostInLibrary top -> {
                for (UUID ownerId : gameData.orderedPlayerIds) {
                    if (!namesPlayer(source, controllerId, ownerId, top.owners())) {
                        continue;
                    }
                    for (Card card : List.copyOf(gameData.playerDecks.getOrDefault(ownerId, List.of()))) {
                        if (!card.isToken() && (top.filter() == null || reads.matches(card, ownerId, top.filter()))) {
                            named.add(new LocatedCard(card, Zone.LIBRARY, ownerId));
                            break;
                        }
                    }
                }
            }
            case CardSubject.ExiledAsCost ignored -> {
                if (source != null) {
                    for (Card card : source.getDelvedCards()) {
                        ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
                        named.add(new LocatedCard(card, Zone.EXILE, exiled != null ? exiled.ownerId() : controllerId));
                    }
                }
            }
            default -> throw new UnsupportedOperationException("No static form for " + cards + " yet");
        }
        return named;
    }

    private static void namedExiledCards(GameData gameData, Permanent source, UUID controllerId, CardSubject cards,
                                         ExiledCardReads reads, List<LocatedCard> named) {
        List<ExiledCardEntry> exiled;
        synchronized (gameData.exiledCards) {
            exiled = List.copyOf(gameData.exiledCards);
        }
        for (ExiledCardEntry held : exiled) {
            if (namesExiledCard(source, controllerId, held, cards, reads)) {
                named.add(new LocatedCard(held.card(), Zone.EXILE, held.ownerId()));
            }
        }
    }

    /**
     * Whether {@code exiled} was exiled with the permanent {@code sourceId}, by its ability of {@code abilityLink} when
     * one is given (CR 607.2a).
     */
    private static boolean exiledWithSourceBy(ExiledCardEntry exiled, UUID sourceId, String abilityLink) {
        return sourceId.equals(exiled.sourcePermanentId())
                && (abilityLink == null || abilityLink.equals(exiled.abilityLink()));
    }

    /** How {@link #namesExiledCard} and {@link #namedCards} judge a card against a subject's filter. */
    @FunctionalInterface
    public interface ExiledCardReads {
        boolean matches(Card card, UUID ownerId, CardPredicate filter);
    }

    /**
     * The characteristics the static membership form reads about the permanent it judges, answered by the static
     * effect handlers' recursion-safe reads of the layered pass in progress.
     */
    public interface StaticReads {

        /** Whether {@code permanent} is a creature as the static pass has it so far. */
        boolean isCreature(GameData gameData, Permanent permanent);

        /** Whether {@code permanent} matches a single characteristic predicate of a subject's set. */
        boolean matchesLeaf(Permanent permanent, PermanentPredicate leaf);

        /** Whether {@code permanent} matches an effect's own filter, judged for the context's source. */
        boolean matchesFilter(StaticEffectContext context, Permanent permanent, PermanentPredicate filter);

        /**
         * Reads for a permanent the caller has already found to be a creature (a creature entering, an attacking
         * creature): a set's creature part holds, any other characteristic part does not, and no filter is read.
         */
        static StaticReads knownCreature() {
            return KnownCharacteristic.CREATURE;
        }

        /** As {@link #knownCreature()}, for a permanent the caller has already found to be a land. */
        static StaticReads knownLand() {
            return KnownCharacteristic.LAND;
        }
    }

    /** The characteristic a caller already judged; see {@link StaticReads#knownCreature()}. */
    private enum KnownCharacteristic implements StaticReads {
        CREATURE,
        LAND;

        @Override
        public boolean isCreature(GameData gameData, Permanent permanent) {
            return this == CREATURE;
        }

        @Override
        public boolean matchesLeaf(Permanent permanent, PermanentPredicate leaf) {
            return this == LAND && leaf instanceof PermanentIsLandPredicate;
        }

        @Override
        public boolean matchesFilter(StaticEffectContext context, Permanent permanent, PermanentPredicate filter) {
            throw new UnsupportedOperationException("A known characteristic reads no filter");
        }
    }

    /** {@code targetControllerId} is whose battlefield the target is on when the caller knows it, else {@code null}. */
    private boolean namesUnfiltered(GameData gameData, Permanent source, UUID sourceId, UUID controllerId,
                                    boolean onSourcesBattlefield, UUID targetControllerId, Permanent target,
                                    PermanentSubject subject, StaticReads reads) {
        return switch (subject) {
            case PermanentSubject.Source ignored -> target.getId().equals(sourceId);
            case PermanentSubject.Attached ignored -> source != null
                    && target.getId().equals(permanentAttachment(gameData, source));
            case PermanentSubject.AttachedOrSource ignored -> {
                if (source == null) {
                    yield false;
                }
                UUID host = permanentAttachment(gameData, source);
                yield target.getId().equals(host != null ? host : sourceId);
            }
            case PermanentSubject.Named named -> target.getId().equals(named.permanentId());
            case PermanentSubject.ControlledBy controlledBy ->
                    controlledByIncludes(gameData, source, controllerId, onSourcesBattlefield, targetControllerId,
                            target, controlledBy.players())
                            && setFilterMatches(gameData, source, sourceId, target, controlledBy.filter(), reads);
            case PermanentSubject.AllOf allOf -> {
                for (PermanentSubject part : allOf.subjects()) {
                    if (namesUnfiltered(gameData, source, sourceId, controllerId, onSourcesBattlefield,
                            targetControllerId, target, part, reads)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> throw new UnsupportedOperationException(
                    "No one-permanent static form for " + subject + " yet");
        };
    }

    /** Whether {@code target} is on the battlefield of one of the players a static set names. */
    private boolean controlledByIncludes(GameData gameData, Permanent source, UUID controllerId,
                                         boolean onSourcesBattlefield, UUID targetControllerId, Permanent target,
                                         PlayerSubject players) {
        return switch (players) {
            case PlayerSubject.You ignored -> onSourcesBattlefield;
            case PlayerSubject.Each each -> each.relation() == PlayerSubject.Relation.ALL || !onSourcesBattlefield;
            case PlayerSubject.EnchantedPlayer ignored -> {
                if (source == null || source.getAttachedTo() == null) {
                    yield false;
                }
                if (targetControllerId != null) {
                    yield targetControllerId.equals(source.getAttachedTo());
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(source.getAttachedTo());
                yield battlefield != null && battlefield.contains(target);
            }
            default -> {
                if (source == null) {
                    throw new UnsupportedOperationException("No static form without a source for " + players);
                }
                for (UUID playerId : players(new Context(gameData, null, null, source, controllerId), players)) {
                    if (targetControllerId != null) {
                        if (playerId.equals(targetControllerId)) {
                            yield true;
                        }
                        continue;
                    }
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield != null && battlefield.contains(target)) {
                        yield true;
                    }
                }
                yield false;
            }
        };
    }

    /**
     * A static set's own predicate: the structural parts (the source, its pair, tapped) are judged here, the
     * characteristic parts by {@code reads}, in the predicate's order.
     */
    private static boolean setFilterMatches(GameData gameData, Permanent source, UUID sourceId, Permanent target,
                                            PermanentPredicate predicate, StaticReads reads) {
        return switch (predicate) {
            case null -> true;
            case PermanentTruePredicate ignored -> true;
            case PermanentAllOfPredicate allOf -> {
                for (PermanentPredicate part : allOf.predicates()) {
                    if (!setFilterMatches(gameData, source, sourceId, target, part, reads)) {
                        yield false;
                    }
                }
                yield true;
            }
            case PermanentNotPredicate not -> !setFilterMatches(gameData, source, sourceId, target, not.predicate(),
                    reads);
            case PermanentIsSourcePermanentPredicate ignored -> target.getId().equals(sourceId);
            case PermanentIsSourceOrPairedPredicate ignored -> target.getId().equals(sourceId)
                    || source != null && target.getId().equals(source.getPairedWithId());
            case PermanentIsTappedPredicate ignored -> target.isTapped();
            case PermanentIsCreaturePredicate ignored -> reads.isCreature(gameData, target);
            default -> reads.matchesLeaf(target, predicate);
        };
    }

    private static void requireStatic(boolean needsStackEntry, Object subject) {
        if (needsStackEntry) {
            throw new IllegalArgumentException("A static ability can't use a subject that reads a stack entry: "
                    + subject);
        }
    }

    private List<Permanent> livePermanents(Context ctx, PermanentSubject subject) {
        if (subject instanceof PermanentSubject.ControlledBy controlledBy) {
            return controlledBy(ctx, controlledBy);
        }
        if (subject instanceof PermanentSubject.OwnedBy ownedBy) {
            return ownedBy(ctx, ownedBy);
        }
        if (subject instanceof PermanentSubject.AttachmentsOf attachmentsOf) {
            return attachmentsOf(ctx, attachmentsOf);
        }
        if (subject instanceof PermanentSubject.AttachedToPlayers attached) {
            return attachedToPlayers(ctx, attached);
        }
        List<Permanent> live = new ArrayList<>();
        for (UUID id : ids(ctx, subject)) {
            Permanent permanent = gameQueryService.findPermanentById(ctx.gameData(), id);
            if (permanent != null) {
                live.add(permanent);
            }
        }
        return live;
    }

    private List<UUID> ids(Context ctx, PermanentSubject subject) {
        return switch (subject) {
            case PermanentSubject.Source ignored -> listOf(ctx.sourceId());
            case PermanentSubject.Target target -> targetIds(ctx, target.group()).stream()
                    .filter(id -> !ctx.gameData().playerIds.contains(id))
                    .toList();
            case PermanentSubject.AllTargets ignored -> allTargetIds(ctx.entry()).stream()
                    .filter(id -> !ctx.gameData().playerIds.contains(id))
                    .toList();
            case PermanentSubject.Attached attached -> attachedIds(ctx, attached);
            case PermanentSubject.AttachedOrSource ignored -> listOf(attachedOrSourceId(ctx));
            case PermanentSubject.AttachedAtTrigger ignored -> listOf(ctx.entry().getAttachedPermanentSnapshot() == null
                    ? null : ctx.entry().getAttachedPermanentSnapshot().getId());
            case PermanentSubject.Triggering ignored -> listOf(ctx.entry().getTriggeringPermanentId());
            case PermanentSubject.TriggeringAll ignored -> ctx.entry().getTriggeringPermanentIds().isEmpty()
                    ? listOf(ctx.entry().getTriggeringPermanentId())
                    : List.copyOf(ctx.entry().getTriggeringPermanentIds());
            case PermanentSubject.TriggeringOrSource ignored -> listOf(ctx.entry().getTriggeringPermanentId() != null
                    ? ctx.entry().getTriggeringPermanentId() : ctx.sourceId());
            case PermanentSubject.TriggeringCard ignored -> listOf(triggeringCardPermanentId(ctx));
            case PermanentSubject.TargetSpell ignored -> List.of();
            case PermanentSubject.TargetAbilitySource abilitySource -> targetAbilitySourceIds(ctx, abilitySource);
            case PermanentSubject.TriggeringSpell ignored -> List.of();
            case PermanentSubject.Returned ignored -> listOf(returnedPermanentId(ctx));
            case PermanentSubject.ReturnedThisResolution ignored -> List.copyOf(ctx.entry().getReturnedPermanentIds());
            case PermanentSubject.ExiledThisResolution ignored -> listOf(ctx.entry().getTargetId());
            case PermanentSubject.Granter granter -> listOf(granter.boundPermanentId() != null
                    ? granter.boundPermanentId() : ctx.entry().getGrantingPermanentId());
            case PermanentSubject.Named named -> listOf(named.permanentId());
            case PermanentSubject.ControlledBy controlledBy -> controlledBy(ctx, controlledBy).stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.OwnedBy ownedBy -> ownedBy(ctx, ownedBy).stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.AttachmentsOf attachmentsOf -> attachmentsOf(ctx, attachmentsOf).stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.AttachedToPlayers attached -> attachedToPlayers(ctx, attached).stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.AllOf allOf -> {
                Set<UUID> union = new LinkedHashSet<>();
                for (PermanentSubject part : allOf.subjects()) {
                    union.addAll(ids(ctx, part));
                }
                yield List.copyOf(union);
            }
            case PermanentSubject.Except except -> {
                Set<UUID> excluded = new LinkedHashSet<>(ids(ctx, except.excluded()));
                yield ids(ctx, except.permanents()).stream().filter(id -> !excluded.contains(id)).toList();
            }
            case PermanentSubject.OneOf oneOf -> throw new IllegalArgumentException(
                    "A choice between subjects is made by the verb's handler, which resolves each option: " + oneOf);
            case PermanentSubject.TriggeringAttackers ignored -> ctx.entry().getAttackingPermanentSnapshots().stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.TriggeringSpellTargets ignored -> triggeringSpellTargetIds(ctx);
            case PermanentSubject.CreatedThisResolution ignored -> List.copyOf(ctx.entry().getCreatedPermanentIds());
            case PermanentSubject.Searched ignored -> searchedIds(ctx.entry());
            case PermanentSubject.Picked picked -> pickedIds(ctx, picked);
            case PermanentSubject.AttachedAndSharingCreatureType ignored -> attachedAndSharingCreatureTypeIds(ctx);
            case PermanentSubject.ThisWay thisWay -> thisWayIds(ctx, thisWay);
            case PermanentSubject.DepartedThisWay departed -> departedThisWayIds(ctx, departed);
            case PermanentSubject.TargetDeclaredElsewhere ignored -> targetIds(ctx, null);
            case PermanentSubject.PaidAsCost ignored -> paidAsCostIds(ctx.entry());
            case PermanentSubject.Convoked ignored -> List.copyOf(ctx.entry().getConvokeCreatureIds());
            case PermanentSubject.BandmatesOf bandmates -> bandmateIds(ctx, bandmates.permanent());
            case PermanentSubject.WithCreaturesSharingAColor sharing -> sharingAColorIds(ctx, sharing.permanent());
            case PermanentSubject.OtherPermanentsSharing sharing -> otherPermanentsSharingIds(ctx, sharing);
            case PermanentSubject.Voted voted -> voted(ctx.entry(), StepOutcome.Vote.PERMANENT, voted.tally());
        };
    }

    /**
     * The candidates of one kind the entry's most recent vote ({@code VoteEffect}) named, by the votes each received
     * ({@link VoteEffect.Tally#MOST}: the most any received, at least one; {@link VoteEffect.Tally#ANY}: one or more), in
     * the order their first votes were cast.
     */
    private static List<UUID> voted(StackEntry entry, String kindPrefix, VoteEffect.Tally tally) {
        Map<UUID, Integer> received = votesReceived(entry, kindPrefix);
        int most = received.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return received.entrySet().stream()
                .filter(cast -> tally == VoteEffect.Tally.ANY || cast.getValue() == most)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static Map<UUID, Integer> votesReceived(StackEntry entry, String kindPrefix) {
        Map<UUID, Integer> received = new LinkedHashMap<>();
        for (StepOutcome.Vote vote : entry.getResolutionResults().outcome(StepKind.VOTE).votes()) {
            if (vote.candidateId() != null && vote.choice().startsWith(kindPrefix)) {
                received.merge(vote.candidateId(), 1, Integer::sum);
            }
        }
        return received;
    }

    /**
     * {@link PermanentSubject.TargetAbilitySource}: the source permanent of the ability the entry targets while it is on
     * the stack, else the one the counter step recorded; only while it is on the battlefield and matches the filter now.
     */
    private List<UUID> targetAbilitySourceIds(Context ctx, PermanentSubject.TargetAbilitySource abilitySource) {
        StackEntry entry = ctx.entry();
        UUID targetId = entry.getTargetId();
        UUID sourceId = entry.getCounteredAbilitySourceId();
        if (targetId != null) {
            for (StackEntry onStack : ctx.gameData().stack) {
                if (targetId.equals(onStack.getTargetableId())) {
                    sourceId = StackEntryType.ABILITIES.contains(onStack.getEntryType())
                            ? onStack.getSourcePermanentId() : null;
                    break;
                }
            }
        }
        if (sourceId == null) {
            return List.of();
        }
        Permanent permanent = gameQueryService.findPermanentById(ctx.gameData(), sourceId);
        if (permanent == null || abilitySource.filter() != null && !predicateEvaluationService
                .matchesPermanentPredicate(permanent, abilitySource.filter(), filterContext(ctx))) {
            return List.of();
        }
        return List.of(sourceId);
    }

    /**
     * {@link PermanentSubject.ThisWay}: the permanents the step recorded, those on the battlefield matching the filter
     * now (with no filter, every recorded id; a gone one names nothing when the permanents are looked up).
     */
    private List<UUID> thisWayIds(Context ctx, PermanentSubject.ThisWay thisWay) {
        List<UUID> recorded = ctx.entry().getResolutionResults().outcome(thisWay.kind()).objectIds()
                .stream().distinct().toList();
        if (thisWay.filter() == null) {
            return recorded;
        }
        FilterContext filterContext = filterContext(ctx);
        List<UUID> matching = new ArrayList<>();
        for (UUID id : recorded) {
            Permanent permanent = gameQueryService.findPermanentById(ctx.gameData(), id);
            if (permanent != null
                    && predicateEvaluationService.matchesPermanentPredicate(permanent, thisWay.filter(), filterContext)) {
                matching.add(id);
            }
        }
        return matching;
    }

    /**
     * {@link PermanentSubject.Picked}: the permanents the entry last recorded as its picks, each once, those on the
     * battlefield matching the filter now (with no filter, every recorded id; a gone one names nothing when the
     * permanents are looked up).
     */
    private List<UUID> pickedIds(Context ctx, PermanentSubject.Picked picked) {
        List<UUID> recorded = ctx.entry().getResolutionResults().pickedIds().stream().distinct().toList();
        if (picked.filter() == null) {
            return recorded;
        }
        FilterContext filterContext = filterContext(ctx);
        List<UUID> matching = new ArrayList<>();
        for (UUID id : recorded) {
            Permanent permanent = gameQueryService.findPermanentById(ctx.gameData(), id);
            if (permanent != null
                    && predicateEvaluationService.matchesPermanentPredicate(permanent, picked.filter(), filterContext)) {
                matching.add(id);
            }
        }
        return matching;
    }

    /**
     * {@link PermanentSubject.PaidAsCost}: the permanent the entry recorded as sacrificed (to its cost, or by the ability
     * itself, as exploit does), then the permanents its cost tracked as exiled or tapped, each once.
     */
    private static List<UUID> paidAsCostIds(StackEntry entry) {
        List<UUID> ids = new ArrayList<>();
        if (entry.getSacrificedPermanentSnapshot() != null) {
            ids.add(entry.getSacrificedPermanentSnapshot().getId());
        }
        if (entry.getChosenCostPermanentIds() != null) {
            ids.addAll(entry.getChosenCostPermanentIds());
        }
        return ids.stream().distinct().toList();
    }

    /**
     * {@link PermanentSubject.DepartedThisWay}: the permanents the step recorded that are no longer on the battlefield,
     * each once.
     */
    private List<UUID> departedThisWayIds(Context ctx, PermanentSubject.DepartedThisWay departed) {
        return ctx.entry().getResolutionResults().outcome(departed.kind()).objectIds().stream()
                .distinct()
                .filter(id -> gameQueryService.findPermanentById(ctx.gameData(), id) == null)
                .toList();
    }

    /**
     * {@link PermanentSubject.Attached}: the host; for "enchanted creature" as an effect resolves, only while the host
     * on the battlefield is a creature.
     */
    private List<UUID> attachedIds(Context ctx, PermanentSubject.Attached attached) {
        UUID hostId = attachedId(ctx, attached.lastAttached());
        if (attached.creature() && hostId != null && ctx.entry() != null) {
            Permanent host = gameQueryService.findPermanentById(ctx.gameData(), hostId);
            if (host != null && !gameQueryService.isCreature(ctx.gameData(), host)) {
                return List.of();
            }
        }
        return listOf(hostId);
    }

    /**
     * The targets of the spell the trigger event was about, as that spell is on the stack, else as the entry recorded
     * them; with no triggering spell, the targets the entry assigns to the effect.
     */
    private List<UUID> triggeringSpellTargetIds(Context ctx) {
        StackEntry entry = ctx.entry();
        StackEntry triggeringSpell = entry.getTriggeringCardId() == null
                ? null
                : gameQueryService.findStackEntryByCardId(ctx.gameData(), entry.getTriggeringCardId());
        if (triggeringSpell == null) {
            return entry.getTriggeringSpellTargetIds().isEmpty()
                    ? entry.targetsForEffect(ctx.effect()) : entry.getTriggeringSpellTargetIds();
        }
        List<UUID> targetIds = new ArrayList<>();
        if (triggeringSpell.getTargetId() != null) {
            targetIds.add(triggeringSpell.getTargetId());
        }
        targetIds.addAll(triggeringSpell.getDeclaredTargetIds());
        return targetIds;
    }

    /**
     * The other attacking creatures in the attacking band of the one permanent {@code permanent} names (CR 702.22),
     * read as it is, else ({@code source()}) as it last existed; none when it is not attacking in a band.
     */
    private List<UUID> bandmateIds(Context ctx, PermanentSubject permanent) {
        Permanent member;
        if (permanent instanceof PermanentSubject.Source) {
            member = sourceAsItIsOrWas(ctx);
        } else {
            List<Permanent> named = livePermanents(ctx, permanent);
            member = named.isEmpty() ? null : named.getFirst();
        }
        if (member == null || !member.isAttacking() || member.getBandId() == null) {
            return List.of();
        }
        UUID bandId = member.getBandId();
        UUID memberId = member.getId();
        List<UUID> bandmates = new ArrayList<>();
        ctx.gameData().forEachPermanent((playerId, candidate) -> {
            if (candidate.isAttacking() && bandId.equals(candidate.getBandId())
                    && !candidate.getId().equals(memberId)) {
                bandmates.add(candidate.getId());
            }
        });
        return bandmates;
    }

    /**
     * The creature {@code permanent} names and every creature sharing a color with it, in battlefield order; nothing
     * when the named permanent is gone or no longer a creature.
     */
    private List<UUID> sharingAColorIds(Context ctx, PermanentSubject permanent) {
        GameData gameData = ctx.gameData();
        List<Permanent> named = livePermanents(ctx, permanent);
        if (named.isEmpty() || !gameQueryService.isCreature(gameData, named.getFirst())) {
            return List.of();
        }
        Permanent creature = named.getFirst();
        Set<CardColor> colors = gameQueryService.getEffectiveColors(gameData, creature);
        List<UUID> sharing = new ArrayList<>();
        gameData.forEachPermanent((playerId, candidate) -> {
            if (gameQueryService.isCreature(gameData, candidate)
                    && (candidate.getId().equals(creature.getId())
                    || !colors.isEmpty() && gameQueryService.getEffectiveColors(gameData, candidate).stream()
                    .anyMatch(colors::contains))) {
                sharing.add(candidate.getId());
            }
        });
        return sharing;
    }

    /**
     * {@link PermanentSubject.Searched}: every permanent the entry recorded a search as putting onto the battlefield,
     * else the one it recorded as found.
     */
    private static List<UUID> searchedIds(StackEntry entry) {
        return entry.getSearchedPermanentIds().isEmpty()
                ? listOf(entry.getChosenPermanentId()) : List.copyOf(entry.getSearchedPermanentIds());
    }

    /**
     * {@link PermanentSubject.AttachedAndSharingCreatureType}: the host recorded as the ability was activated, as it
     * is, else as the entry last knew it (CR 608.2h), and the creatures sharing a creature type with it.
     */
    private List<UUID> attachedAndSharingCreatureTypeIds(Context ctx) {
        GameData gameData = ctx.gameData();
        StackEntry entry = ctx.entry();
        UUID hostId = entry.getTargetId() != null ? entry.getTargetId() : attachedId(ctx);
        if (hostId == null) {
            return List.of();
        }
        Permanent current = gameQueryService.findPermanentById(gameData, hostId);
        Permanent host;
        if (current != null) {
            if (!gameQueryService.isCreature(gameData, current)) {
                return List.of();
            }
            host = current;
        } else {
            Card lastKnown = entry.lastKnownPermanentCard(hostId);
            if (lastKnown == null || !lastKnown.hasType(CardType.CREATURE)) {
                return List.of();
            }
            host = new Permanent(lastKnown);
        }
        List<UUID> named = new ArrayList<>();
        gameData.forEachPermanent((playerId, candidate) -> {
            if (gameQueryService.isCreature(gameData, candidate) && (candidate.getId().equals(hostId)
                    || gameQueryService.shareCreatureType(gameData, host, candidate))) {
                named.add(candidate.getId());
            }
        });
        return named;
    }

    /**
     * {@link PermanentSubject.OtherPermanentsSharing}: the other permanents matching the filter that share the quality
     * with the one live permanent the subject names, while that one matches the filter too.
     */
    private List<UUID> otherPermanentsSharingIds(Context ctx, PermanentSubject.OtherPermanentsSharing sharing) {
        GameData gameData = ctx.gameData();
        List<Permanent> named = livePermanents(ctx, sharing.permanent());
        FilterContext filterContext = filterContext(ctx);
        if (named.isEmpty() || !sharing.sameController() && !predicateEvaluationService.matchesPermanentPredicate(
                named.getFirst(), sharing.filter(), filterContext)) {
            return List.of();
        }
        Permanent shared = named.getFirst();
        UUID sharedController = sharing.sameController()
                ? gameQueryService.findPermanentController(gameData, shared.getId()) : null;
        Predicate<Permanent> shares = switch (sharing.quality()) {
            case COLOR -> {
                Set<CardColor> colors = gameQueryService.getEffectiveColors(gameData, shared);
                yield candidate -> gameQueryService.getEffectiveColors(gameData, candidate).stream()
                        .anyMatch(colors::contains);
            }
            case NAME -> {
                String name = gameQueryService.getEffectiveName(gameData, shared);
                yield candidate -> name != null && (hasAllCardNames(shared) || hasAllCardNames(candidate)
                        ? gameQueryService.getEffectiveName(gameData, candidate) != null
                        : name.equals(gameQueryService.getEffectiveName(gameData, candidate)));
            }
            case CREATURE_TYPE -> candidate -> gameQueryService.shareCreatureType(gameData, candidate, shared);
            case MANA_VALUE -> {
                int manaValue = gameQueryService.getPermanentManaValue(shared);
                yield candidate -> gameQueryService.getPermanentManaValue(candidate) == manaValue;
            }
        };
        List<UUID> others = new ArrayList<>();
        gameData.forEachPermanent((playerId, candidate) -> {
            if (!candidate.getId().equals(shared.getId())
                    && (sharedController == null || sharedController.equals(playerId))
                    && predicateEvaluationService.matchesPermanentPredicate(candidate, sharing.filter(), filterContext)
                    && shares.test(candidate)) {
                others.add(candidate.getId());
            }
        });
        return others;
    }

    /** A face-down permanent has no name, so the all-names characteristic of its card doesn't apply (CR 708.2). */
    private static boolean hasAllCardNames(Permanent permanent) {
        return !permanent.isFaceDown() && permanent.getCard().hasAllCardNames();
    }

    private List<UUID> targetIds(Context ctx, Integer group) {
        StackEntry entry = ctx.entry();
        if (group != null) {
            return entry.targetsForGroup(group);
        }
        List<UUID> targets = entry.targetsForEffect(ctx.effect());
        return targets.isEmpty() ? listOf(entry.getTargetId()) : targets;
    }

    /** Every target of the entry, whichever group it was chosen for; a lone target the entry stores apart. */
    private static List<UUID> allTargetIds(StackEntry entry) {
        return entry.getTargetIds().isEmpty() ? listOf(entry.getTargetId()) : entry.getTargetIds();
    }

    /**
     * "Enchanted creature", "equipped creature": what the source is attached to now while it is on the battlefield
     * (nothing if it is unattached), else what it was attached to as it last existed there (CR 608.2h): the source's
     * last-known snapshot, or, when the entry recorded none, the permanent the source was attached to as the ability
     * triggered. A snapshot left unattached because its host left first (CR 704.5m) names nothing.
     */
    private UUID attachedId(Context ctx) {
        return attachedId(ctx, false);
    }

    /**
     * {@link #attachedId(Context)}; with {@code lastAttached} ("the creature it was last enchanting"), a source that
     * left the battlefield unattached still names the host the entry recorded as the ability triggered or was activated.
     */
    private UUID attachedId(Context ctx, boolean lastAttached) {
        if (ctx.staticSource() != null) {
            return permanentAttachment(ctx.gameData(), ctx.staticSource());
        }
        StackEntry entry = ctx.entry();
        UUID sourceId = entry.getSourcePermanentId();
        Permanent live = sourceId == null ? null : gameQueryService.findPermanentById(ctx.gameData(), sourceId);
        if (live != null) {
            return permanentAttachment(ctx.gameData(), live);
        }
        Permanent snapshot = ctx.sourceSnapshot();
        Permanent attachedSnapshot = entry.getAttachedPermanentSnapshot();
        if (snapshot != null && (!lastAttached || snapshot.getAttachedTo() != null || attachedSnapshot == null)) {
            return permanentAttachment(ctx.gameData(), snapshot);
        }
        return attachedSnapshot == null ? null : attachedSnapshot.getId();
    }

    /**
     * The host of the source while the source (as it is, else as it last existed) is attached to a permanent, else the
     * source itself; with no source known, the host the entry recorded.
     */
    private UUID attachedOrSourceId(Context ctx) {
        if (ctx.staticSource() != null) {
            UUID host = permanentAttachment(ctx.gameData(), ctx.staticSource());
            return host != null ? host : ctx.staticSource().getId();
        }
        StackEntry entry = ctx.entry();
        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(ctx.gameData(), sourceId);
        if (source == null) {
            source = ctx.sourceSnapshot();
        }
        if (source != null) {
            UUID host = permanentAttachment(ctx.gameData(), source);
            return host != null ? host : source.getId();
        }
        Permanent attachedSnapshot = entry.getAttachedPermanentSnapshot();
        return attachedSnapshot == null ? null : attachedSnapshot.getId();
    }

    private static UUID permanentAttachment(GameData gameData, Permanent attachment) {
        UUID attachedTo = attachment.getAttachedTo();
        return attachedTo == null || gameData.playerIds.contains(attachedTo) ? null : attachedTo;
    }

    /**
     * The permanent a preceding "return target card to the battlefield" step made of the entry's targeted card: the
     * entry's target, else its first targeted card, found on the battlefield by its card.
     */
    private UUID returnedPermanentId(Context ctx) {
        StackEntry entry = ctx.entry();
        UUID cardId = entry.getTargetId();
        if (cardId == null && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            cardId = entry.getTargetCardIds().getFirst();
        }
        if (cardId == null) {
            return null;
        }
        for (UUID playerId : ctx.gameData().orderedPlayerIds) {
            List<Permanent> battlefield = ctx.gameData().playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())
                        || permanent.getOriginalCard() != null && cardId.equals(permanent.getOriginalCard().getId())) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }

    /** The permanent the triggering card is on the battlefield (a land played), else none. */
    private static UUID triggeringCardPermanentId(Context ctx) {
        Card card = ctx.entry().triggeringSpellOrCard();
        if (card == null) {
            return null;
        }
        for (UUID playerId : ctx.gameData().orderedPlayerIds) {
            for (Permanent permanent : ctx.gameData().playerBattlefields.getOrDefault(playerId, List.of())) {
                if (card.getId().equals(permanent.getCard().getId())) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }

    private List<Permanent> controlledBy(Context ctx, PermanentSubject.ControlledBy subject) {
        FilterContext filterContext = filterContext(ctx);
        PermanentPredicate filter = ctx.entry() != null && namesTargetCard(subject.filter())
                ? bindTarget(ctx, subject.filter()) : subject.filter();
        List<Permanent> matching = new ArrayList<>();
        for (UUID playerId : players(ctx, subject.players())) {
            List<Permanent> battlefield = ctx.gameData().playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext)) {
                    matching.add(permanent);
                }
            }
        }
        return matching;
    }

    /**
     * Whether {@code predicate} has a leaf that a resolving effect's set binds to the entry's target permanent: a
     * {@link PermanentIsTargetCardPredicate} ("each other creature that player controls") or a
     * {@link PermanentPowerLessThanTargetPredicate} ("with lesser power").
     */
    private static boolean namesTargetCard(PermanentPredicate predicate) {
        return switch (predicate) {
            case PermanentIsTargetCardPredicate ignored -> true;
            case PermanentPowerLessThanTargetPredicate ignored -> true;
            case PermanentAllOfPredicate allOf -> allOf.predicates().stream()
                    .anyMatch(SubjectResolutionService::namesTargetCard);
            case PermanentAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(SubjectResolutionService::namesTargetCard);
            case PermanentNotPredicate not -> namesTargetCard(not.predicate());
            case null, default -> false;
        };
    }

    /** Every matching permanent whose owner is one of the players, whoever controls it. */
    private List<Permanent> ownedBy(Context ctx, PermanentSubject.OwnedBy subject) {
        List<UUID> owners = players(ctx, subject.players());
        if (owners.isEmpty()) {
            return List.of();
        }
        FilterContext filterContext = filterContext(ctx);
        List<Permanent> matching = new ArrayList<>();
        for (UUID playerId : apnapOrder(ctx.gameData())) {
            for (Permanent permanent : ctx.gameData().playerBattlefields.getOrDefault(playerId, List.of())) {
                if (owners.contains(ownerOfLive(ctx.gameData(), permanent))
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, subject.filter(),
                        filterContext)) {
                    matching.add(permanent);
                }
            }
        }
        return matching;
    }

    /** Every matching permanent attached to one of the named permanents that are on the battlefield. */
    private List<Permanent> attachmentsOf(Context ctx, PermanentSubject.AttachmentsOf subject) {
        Set<UUID> hosts = new LinkedHashSet<>();
        for (Permanent host : livePermanents(ctx, subject.permanents())) {
            hosts.add(host.getId());
        }
        if (hosts.isEmpty()) {
            return List.of();
        }
        FilterContext filterContext = filterContext(ctx);
        List<Permanent> matching = new ArrayList<>();
        for (UUID playerId : apnapOrder(ctx.gameData())) {
            for (Permanent permanent : ctx.gameData().playerBattlefields.getOrDefault(playerId, List.of())) {
                if (permanent.getAttachedTo() != null && hosts.contains(permanent.getAttachedTo())
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, subject.filter(),
                        filterContext)) {
                    matching.add(permanent);
                }
            }
        }
        return matching;
    }

    private List<Permanent> attachedToPlayers(Context ctx, PermanentSubject.AttachedToPlayers subject) {
        Set<UUID> hosts = new LinkedHashSet<>(players(ctx, subject.players()));
        if (hosts.isEmpty()) {
            return List.of();
        }
        FilterContext filterContext = filterContext(ctx);
        List<Permanent> matching = new ArrayList<>();
        for (UUID playerId : apnapOrder(ctx.gameData())) {
            for (Permanent permanent : ctx.gameData().playerBattlefields.getOrDefault(playerId, List.of())) {
                if (permanent.getAttachedTo() != null && hosts.contains(permanent.getAttachedTo())
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, subject.filter(),
                        filterContext)) {
                    matching.add(permanent);
                }
            }
        }
        return matching;
    }

    /**
     * What a set's filter knows about the resolving spell or ability: its source card, source permanent (live id and
     * last-known snapshot), controller and X, and what its trigger was about: the defending player (as the entry
     * recorded it when the ability went on the stack, else the player or the protector of the attacked permanent), the
     * triggering player and the triggering permanent ("tap all lands that player controls", "untap all creatures
     * defending player controls").
     */
    private FilterContext filterContext(Context ctx) {
        FilterContext filterContext = FilterContext.of(ctx.gameData())
                .withSourceCardId(ctx.sourceCardId())
                .withSourceControllerId(ctx.controllerId())
                .withSourcePermanentId(ctx.sourceId());
        Permanent sourceSnapshot = ctx.sourceSnapshot();
        if (sourceSnapshot != null) {
            filterContext = filterContext.withSourcePermanentSnapshot(sourceSnapshot);
        }
        StackEntry entry = ctx.entry();
        if (entry != null) {
            filterContext = filterContext.withXValue(entry.getXValue())
                    .withDefendingPlayerId(entry.getDefendingPlayerId() != null ? entry.getDefendingPlayerId()
                            : gameQueryService.defendingPlayerOfAttackTarget(ctx.gameData(), entry.getAttackedTargetId()))
                    .withTriggeringPlayerId(entry.getTriggeringPlayerId())
                    .withTriggeringPermanentId(entry.getTriggeringPermanentId())
                    .withTriggeringPermanentSnapshot(entry.getTriggeringPermanentSnapshot());
        }
        return filterContext;
    }

    private List<UUID> players(Context ctx, PlayerSubject subject) {
        GameData gameData = ctx.gameData();
        List<UUID> players = switch (subject) {
            case PlayerSubject.You ignored -> listOf(ctx.controllerId());
            case PlayerSubject.TargetPlayer target -> targetIds(ctx, target.group()).stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            case PlayerSubject.Each each -> apnapOrder(gameData).stream()
                    .filter(playerId -> each.relation() == PlayerSubject.Relation.ALL
                            || !playerId.equals(ctx.controllerId()))
                    .toList();
            case PlayerSubject.OpponentsOf opponentsOf -> {
                List<UUID> of = players(ctx, opponentsOf.player());
                yield of.isEmpty() ? List.of() : apnapOrder(gameData).stream()
                        .filter(playerId -> !of.contains(playerId))
                        .toList();
            }
            case PlayerSubject.ControllerOf controllerOf -> controllers(ctx, controllerOf.permanent());
            case PlayerSubject.OwnerOf ownerOf -> owners(ctx, ownerOf.permanent());
            case PlayerSubject.OwnersOfCards owners -> locatedCards(gameData, ctx.entry(), ctx.effect(), owners.cards())
                    .stream()
                    .map(LocatedCard::ownerId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            case PlayerSubject.TriggeringPlayer ignored -> listOf(triggeringPlayer(ctx));
            case PlayerSubject.ActivePlayer ignored -> listOf(gameData.activePlayerId);
            case PlayerSubject.DefendingPlayer ignored -> listOf(ctx.entry().getDefendingPlayerId() != null
                    ? ctx.entry().getDefendingPlayerId()
                    : gameQueryService.defendingPlayerOfAttackTarget(gameData, ctx.entry().getAttackedTargetId()));
            case PlayerSubject.EnchantedPlayer ignored -> listOf(enchantedPlayer(ctx));
            case PlayerSubject.ChosenPlayer ignored -> listOf(chosenPlayer(ctx));
            case PlayerSubject.ModeChooser ignored -> listOf(ctx.entry().getModeChooserId()).stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            case PlayerSubject.AcceptedPlayers ignored -> ctx.entry().getAcceptedPlayerIds().stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            case PlayerSubject.PlayersDealtDamage ignored -> ctx.entry().getPlayersDealtDamageThisResolution().stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            case PlayerSubject.PlayersThisWay thisWay -> playersThisWay(ctx, thisWay).stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            case PlayerSubject.SubgameWinners ignored -> {
                if (ctx.entry().getSubgameResult() == null) {
                    throw new IllegalStateException("Missing subgame result");
                }
                yield ctx.entry().getSubgameResult().winners().stream()
                        .filter(gameData.playerIds::contains)
                        .toList();
            }
            case PlayerSubject.ChosenPlayers ignored -> {
                Permanent source = sourceAsItIsOrWas(ctx);
                yield source == null ? List.of() : source.getRememberedTargetPlayerIds().stream()
                        .filter(gameData.playerIds::contains)
                        .toList();
            }
            case PlayerSubject.Except except -> {
                List<UUID> excluded = players(ctx, except.excluded());
                yield players(ctx, except.players()).stream()
                        .filter(playerId -> !excluded.contains(playerId))
                        .toList();
            }
            case PlayerSubject.AllOf allOf -> {
                Set<UUID> union = new LinkedHashSet<>();
                for (PlayerSubject part : allOf.subjects()) {
                    union.addAll(players(ctx, part));
                }
                yield List.copyOf(union);
            }
            case PlayerSubject.Voted voted -> {
                Map<UUID, Integer> received = votesReceived(ctx.entry(), StepOutcome.Vote.PLAYER);
                yield voted.tally() == VoteEffect.Tally.NONE
                        ? apnapOrder(gameData).stream().filter(playerId -> !received.containsKey(playerId)).toList()
                        : voted(ctx.entry(), StepOutcome.Vote.PLAYER, voted.tally()).stream()
                                .filter(gameData.playerIds::contains)
                                .toList();
            }
            case PlayerSubject.Matching matching -> players(ctx, matching.players()).stream()
                    .filter(playerId -> targetLegalityService.matchesPlayerPredicate(gameData, ctx.controllerId(),
                            playerId, matching.filter(), ctx.sourceId(),
                            ctx.entry() == null ? null : ctx.entry().getTriggeringPlayerId()))
                    .toList();
        };
        return inApnapOrder(gameData, players);
    }

    /**
     * {@link PlayerSubject.PlayersThisWay}: the players the step recorded; with a card filter the owners of the recorded
     * cards matching it where they are now, found in any zone (a discarded card a replacement put onto the battlefield
     * or into a library too); with a permanent filter the controllers, current or last-known (CR 608.2h), of the
     * recorded permanents matching it as they are, or as they last existed on the battlefield.
     */
    private List<UUID> playersThisWay(Context ctx, PlayerSubject.PlayersThisWay thisWay) {
        GameData gameData = ctx.gameData();
        StepOutcome outcome = ctx.entry().getResolutionResults().outcome(thisWay.kind());
        if (thisWay.cardFilter() != null) {
            CardPredicate filter = bindCardSubjects(gameData, ctx.entry(), ctx.effect(), thisWay.cardFilter());
            List<UUID> owners = new ArrayList<>();
            for (UUID cardId : new LinkedHashSet<>(outcome.objectIds())) {
                LocatedCard located = anywhere(gameData, cardId);
                if (located != null && cardMatches(gameData, ctx.entry(), located.card(), located.ownerId(), filter)) {
                    owners.add(located.ownerId());
                }
            }
            return owners;
        }
        if (thisWay.permanentFilter() != null) {
            FilterContext filterContext = filterContext(ctx);
            List<UUID> controllers = new ArrayList<>();
            for (UUID permanentId : new LinkedHashSet<>(outcome.objectIds())) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                if (permanent == null) {
                    permanent = ConditionEvaluationService.lastKnownPermanent(gameData, permanentId);
                }
                if (permanent != null && predicateEvaluationService.matchesPermanentPredicate(permanent,
                        thisWay.permanentFilter(), filterContext)) {
                    UUID controllerId = controllerOf(ctx, permanentId);
                    if (controllerId == null) {
                        controllerId = ctx.entry().getResolutionResults().departures().stream()
                                .filter(departure -> permanentId.equals(departure.permanentId()))
                                .map(DepartureResult::lastKnownControllerId)
                                .filter(Objects::nonNull)
                                .findFirst().orElse(null);
                    }
                    if (controllerId != null) {
                        controllers.add(controllerId);
                    }
                }
            }
            return controllers;
        }
        return outcome.playerIds();
    }

    /**
     * The card {@code cardId} in whatever zone it is in now, with the player whose zone that is: its owner for a hand,
     * library, graveyard or exile (CR 400.3), its controller on the battlefield; {@code null} when no zone holds it.
     */
    private LocatedCard anywhere(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (cardId.equals(permanent.getCard().getId())) {
                    return new LocatedCard(permanent.getCard(), Zone.BATTLEFIELD, playerId);
                }
            }
            for (Zone zone : List.of(Zone.HAND, Zone.LIBRARY, Zone.GRAVEYARD, Zone.EXILE)) {
                List<Card> cards = switch (zone) {
                    case HAND -> gameData.playerHands.get(playerId);
                    case LIBRARY -> gameData.playerDecks.get(playerId);
                    case GRAVEYARD -> gameData.playerGraveyards.get(playerId);
                    default -> gameData.getPlayerExiledCards(playerId);
                };
                if (cards == null) {
                    continue;
                }
                for (Card card : cards) {
                    if (cardId.equals(card.getId())) {
                        return new LocatedCard(card, zone, playerId);
                    }
                }
            }
        }
        return null;
    }

    /** "Its controller": current controller, else last-known (CR 608.2h). */
    private List<UUID> controllers(Context ctx, PermanentSubject permanent) {
        if (permanent instanceof PermanentSubject.Triggering) {
            return listOf(gameQueryService.findTriggeringPermanentController(ctx.gameData(), ctx.entry()));
        }
        if (permanent instanceof PermanentSubject.TargetSpell) {
            return listOf(targetSpellController(ctx));
        }
        if (permanent instanceof PermanentSubject.TriggeringSpell) {
            StackEntry triggeringSpell = CopySupport.triggeringSpell(ctx.gameData(), ctx.entry());
            return listOf(triggeringSpell == null ? null : triggeringSpell.getControllerId());
        }
        List<UUID> controllers = new ArrayList<>();
        for (UUID id : ids(ctx, permanent)) {
            controllers.add(controllerOf(ctx, id));
        }
        return controllers.stream().filter(Objects::nonNull).toList();
    }

    /**
     * "That spell's controller": the controller of the spell the entry targets while it is on the stack; once an
     * earlier step of this resolution countered it, the controller recorded as it was countered; else the controller
     * the entry recorded as it removed it. A counter-unless-pays rider's entry carries no target, only the recorded
     * controller (Frightful Delusion's paid branch).
     */
    private static UUID targetSpellController(Context ctx) {
        StackEntry entry = ctx.entry();
        UUID spellCardId = entry.getTargetId();
        if (spellCardId != null) {
            for (StackEntry onStack : ctx.gameData().stack) {
                if (spellCardId.equals(onStack.getTargetableId())) {
                    return onStack.getControllerId();
                }
            }
        }
        UUID countered = entry.getCounteredSpellControllerId();
        if (countered != null || spellCardId == null) {
            return countered;
        }
        return entry.lastKnownPermanentController(spellCardId);
    }

    private UUID controllerOf(Context ctx, UUID permanentId) {
        UUID current = gameQueryService.findPermanentController(ctx.gameData(), permanentId);
        if (current != null || ctx.entry() == null) {
            return current;
        }
        UUID lastKnown = ctx.entry().lastKnownPermanentController(permanentId);
        if (lastKnown == null && permanentId.equals(ctx.entry().getTriggeringPermanentId())) {
            lastKnown = ctx.entry().getTriggeringPermanentControllerId();
        }
        if (lastKnown == null && permanentId.equals(ctx.entry().getSourcePermanentId())) {
            lastKnown = ctx.entry().getSourcePermanentControllerId();
        }
        if (lastKnown == null) {
            // A permanent that left before this entry resolved: the controller it had as it left (CR 608.2h).
            lastKnown = ctx.gameData().departedPermanentSnapshots.values().stream()
                    .filter(departed -> permanentId.equals(departed.getId()))
                    .findFirst()
                    .map(departed -> ctx.gameData().departedPermanentControllerIds.get(departed.getCard().getId()))
                    .orElse(null);
        }
        return lastKnown;
    }

    /**
     * "Its owner": the owner of its card as it is or last was; an unrecorded owner as the class Javadoc says. The owner
     * of the triggering card is read from the card wherever it is (a card's owner never changes).
     */
    private List<UUID> owners(Context ctx, PermanentSubject permanent) {
        if (permanent instanceof PermanentSubject.TriggeringCard) {
            return listOf(triggeringCardOwner(ctx));
        }
        List<UUID> owners = new ArrayList<>();
        for (UUID id : ids(ctx, permanent)) {
            owners.add(ownerOf(ctx, id));
        }
        return owners.stream().filter(Objects::nonNull).toList();
    }

    /**
     * The owner of the spell cast or land played; else of the card the event put into a graveyard or moved, recorded as
     * it happened; else of the permanent that entered, as it is or as it last existed (CR 608.2h).
     */
    private UUID triggeringCardOwner(Context ctx) {
        UUID owner = gameQueryService.findTriggeringCardOwner(ctx.gameData(), ctx.entry());
        if (owner == null) {
            owner = ctx.entry().getTriggeringPermanentOwnerId();
        }
        return owner != null ? owner : gameQueryService.findTriggeringPermanentOwner(ctx.gameData(), ctx.entry());
    }

    private UUID ownerOf(Context ctx, UUID permanentId) {
        GameData gameData = ctx.gameData();
        Permanent live = gameQueryService.findPermanentById(gameData, permanentId);
        if (live != null) {
            return ownerOfLive(gameData, live);
        }
        Card lastKnownCard = lastKnownCard(ctx, permanentId);
        UUID owner = lastKnownCard == null ? null : gameData.ownerOf(lastKnownCard);
        if (owner == null) {
            owner = controllerOf(ctx, permanentId);
        }
        if (owner == null && ctx.entry() != null && permanentId.equals(ctx.entry().getSourcePermanentId())) {
            // The source's own ability: the controller the ability got from its source.
            owner = ctx.entry().getControllerId();
        }
        return owner;
    }

    /** The owner of a permanent on the battlefield: its card's owner, else the player it entered under. */
    private static UUID ownerOfLive(GameData gameData, Permanent permanent) {
        Card card = permanent.getOriginalCard() != null ? permanent.getOriginalCard() : permanent.getCard();
        UUID owner = gameData.ownerOf(card);
        return owner != null ? owner : gameData.defaultControllerOf(permanent.getId());
    }

    private static Card lastKnownCard(Context ctx, UUID permanentId) {
        if (ctx.entry() == null) {
            return null;
        }
        Card card = ctx.entry().lastKnownPermanentCard(permanentId);
        if (card == null && permanentId.equals(ctx.entry().getSourcePermanentId())) {
            card = ctx.entry().getCard();
        }
        return card;
    }

    /**
     * "That player": the player the entry recorded the trigger event was about, else the player most trigger
     * collectors still record as the entry's non-targeting target.
     */
    private UUID triggeringPlayer(Context ctx) {
        StackEntry entry = ctx.entry();
        if (entry.getTriggeringPlayerId() != null) {
            return entry.getTriggeringPlayerId();
        }
        UUID targetId = entry.getTargetId();
        return targetId != null && ctx.gameData().playerIds.contains(targetId) ? targetId : null;
    }

    /**
     * "The chosen player": the player the source remembered as it entered, as it is or last was; while the enters
     * ability that chooses the player ({@link RememberTargetPlayerEffect}) is still on the stack, that ability's target,
     * so a source that left before it resolved still names the player (Laquatus's Champion's ruling). A spell's chosen
     * player is the one an earlier effect of the spell chose onto the entry.
     */
    private UUID chosenPlayer(Context ctx) {
        Permanent source = sourceAsItIsOrWas(ctx);
        if (source != null && source.getRememberedTargetPlayerId() != null) {
            return source.getRememberedTargetPlayerId();
        }
        UUID sourcePermanentId = ctx.sourceId();
        if (ctx.entry() == null) {
            return null;
        }
        if (sourcePermanentId == null) {
            // A spell has no source to remember a player: its chosen player is the one an earlier effect of the same
            // spell chose onto the entry (Backdraft).
            UUID targetId = ctx.entry().getTargetId();
            return targetId != null && ctx.gameData().playerIds.contains(targetId) ? targetId : null;
        }
        return ctx.gameData().stack.stream()
                .filter(pending -> sourcePermanentId.equals(pending.getSourcePermanentId()))
                .filter(pending -> pending.getEffectsToResolve().stream()
                        .anyMatch(RememberTargetPlayerEffect.class::isInstance))
                .map(StackEntry::getTargetId)
                .filter(ctx.gameData().playerIds::contains)
                .findFirst()
                .orElse(null);
    }

    /**
     * "Enchanted player": the player the source Curse is attached to now, or as it last existed; for an entry whose
     * source never recorded it, the player the trigger recorded as the entry's non-targeting target.
     */
    private UUID enchantedPlayer(Context ctx) {
        Permanent source = sourceAsItIsOrWas(ctx);
        UUID attachedTo = source == null ? null : source.getAttachedTo();
        if (attachedTo != null && ctx.gameData().playerIds.contains(attachedTo)) {
            return attachedTo;
        }
        if (source != null || ctx.entry() == null) {
            return null;
        }
        UUID targetId = ctx.entry().getTargetId();
        return targetId != null && ctx.gameData().playerIds.contains(targetId) ? targetId : null;
    }

    /** The players in APNAP order (CR 101.4): the active player, then the others in turn order. */
    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> seats = List.copyOf(gameData.orderedPlayerIds);
        int active = gameData.activePlayerId == null ? -1 : seats.indexOf(gameData.activePlayerId);
        if (active <= 0) {
            return seats;
        }
        List<UUID> order = new ArrayList<>(seats.size());
        order.addAll(seats.subList(active, seats.size()));
        order.addAll(seats.subList(0, active));
        return order;
    }

    private static List<UUID> inApnapOrder(GameData gameData, List<UUID> players) {
        if (players.size() < 2) {
            return players;
        }
        List<UUID> order = apnapOrder(gameData);
        return players.stream()
                .distinct()
                .sorted(Comparator.comparingInt(order::indexOf))
                .toList();
    }

    private static List<UUID> listOf(UUID id) {
        return id == null ? List.of() : List.of(id);
    }

    /** The source permanent as it is on the battlefield, else as it last existed there. */
    private Permanent sourceAsItIsOrWas(Context ctx) {
        if (ctx.staticSource() != null) {
            return ctx.staticSource();
        }
        UUID sourceId = ctx.entry().getSourcePermanentId();
        if (sourceId == null) {
            return null;
        }
        Permanent live = gameQueryService.findPermanentById(ctx.gameData(), sourceId);
        return live != null ? live : ctx.sourceSnapshot();
    }

    /**
     * What one resolution of a subject reads: a resolving entry and its effect, or a static ability's source; and the
     * controller "you" means.
     */
    private record Context(GameData gameData, StackEntry entry, CardEffect effect, Permanent staticSource,
                           UUID controllerId) {

        UUID sourceId() {
            return staticSource != null ? staticSource.getId() : entry.getSourcePermanentId();
        }

        UUID sourceCardId() {
            if (staticSource != null) {
                return staticSource.getCard().getId();
            }
            return entry.getCard() == null ? null : entry.getCard().getId();
        }

        /** The static source, or the entry's last-known snapshot of its source permanent. */
        Permanent sourceSnapshot() {
            if (staticSource != null) {
                return staticSource;
            }
            Permanent snapshot = entry.getSourcePermanentSnapshot();
            return snapshot != null && snapshot.getId().equals(entry.getSourcePermanentId()) ? snapshot : null;
        }
    }
}
