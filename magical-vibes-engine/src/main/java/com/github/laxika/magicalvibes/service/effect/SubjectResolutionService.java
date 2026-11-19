package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The only code that turns a {@link PermanentSubject} or {@link PlayerSubject} into permanents or players. A handler
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

    /** The players {@code subject} names, in APNAP order, for a resolving effect. */
    public List<UUID> players(GameData gameData, StackEntry entry, CardEffect effect, PlayerSubject subject) {
        return players(new Context(gameData, entry, effect, null, entry.getControllerId()), subject);
    }

    /** The first player {@code subject} names, or {@code null}. */
    public UUID player(GameData gameData, StackEntry entry, CardEffect effect, PlayerSubject subject) {
        List<UUID> players = players(gameData, entry, effect, subject);
        return players.isEmpty() ? null : players.getFirst();
    }

    /** The permanents {@code subject} names for a static ability of {@code source}. */
    public List<Permanent> permanents(GameData gameData, Permanent source, PermanentSubject subject) {
        requireStatic(subject.needsStackEntry(), subject);
        return livePermanents(new Context(gameData, null, null, source,
                gameQueryService.findPermanentController(gameData, source.getId())), subject);
    }

    /** The players {@code subject} names, in APNAP order, for a static ability of {@code source}. */
    public List<UUID> players(GameData gameData, Permanent source, PlayerSubject subject) {
        requireStatic(subject.needsStackEntry(), subject);
        return players(new Context(gameData, null, null, source,
                gameQueryService.findPermanentController(gameData, source.getId())), subject);
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
            case PermanentSubject.Attached ignored -> listOf(attachedId(ctx));
            case PermanentSubject.AttachedAtTrigger ignored -> listOf(ctx.entry().getAttachedPermanentSnapshot() == null
                    ? null : ctx.entry().getAttachedPermanentSnapshot().getId());
            case PermanentSubject.Triggering ignored -> listOf(ctx.entry().getTriggeringPermanentId());
            case PermanentSubject.TriggeringCard ignored -> listOf(triggeringCardPermanentId(ctx));
            case PermanentSubject.TargetSpell ignored -> List.of();
            case PermanentSubject.TriggeringSpell ignored -> List.of();
            case PermanentSubject.Returned ignored -> listOf(returnedPermanentId(ctx));
            case PermanentSubject.Granter granter -> listOf(granter.boundPermanentId() != null
                    ? granter.boundPermanentId() : ctx.entry().getGrantingPermanentId());
            case PermanentSubject.ControlledBy controlledBy -> controlledBy(ctx, controlledBy).stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.OwnedBy ownedBy -> ownedBy(ctx, ownedBy).stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.AttachmentsOf attachmentsOf -> attachmentsOf(ctx, attachmentsOf).stream()
                    .map(Permanent::getId)
                    .toList();
            case PermanentSubject.AllOf allOf -> {
                Set<UUID> union = new LinkedHashSet<>();
                for (PermanentSubject part : allOf.subjects()) {
                    union.addAll(ids(ctx, part));
                }
                yield List.copyOf(union);
            }
        };
    }

    private List<UUID> targetIds(Context ctx, Integer group) {
        StackEntry entry = ctx.entry();
        if (group != null) {
            return entry.targetsForGroup(group);
        }
        List<UUID> targets = entry.targetsForEffect(ctx.effect());
        return targets.isEmpty() ? listOf(entry.getTargetId()) : targets;
    }

    /**
     * "Enchanted creature", "equipped creature": what the source is attached to now while it is on the battlefield
     * (nothing if it is unattached), else what it was attached to as it last existed there (CR 608.2h): the source's
     * last-known snapshot, or, when the entry recorded none, the permanent the source was attached to as the ability
     * triggered.
     */
    private UUID attachedId(Context ctx) {
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
        if (snapshot != null) {
            return permanentAttachment(ctx.gameData(), snapshot);
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
        List<Permanent> matching = new ArrayList<>();
        for (UUID playerId : players(ctx, subject.players())) {
            List<Permanent> battlefield = ctx.gameData().playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, subject.filter(),
                        filterContext)) {
                    matching.add(permanent);
                }
            }
        }
        return matching;
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

    /**
     * What a set's filter knows about the resolving spell or ability: its source card, source permanent (live id and
     * last-known snapshot), controller and X.
     */
    private static FilterContext filterContext(Context ctx) {
        FilterContext filterContext = FilterContext.of(ctx.gameData())
                .withSourceCardId(ctx.sourceCardId())
                .withSourceControllerId(ctx.controllerId())
                .withSourcePermanentId(ctx.sourceId());
        Permanent sourceSnapshot = ctx.sourceSnapshot();
        if (sourceSnapshot != null) {
            filterContext = filterContext.withSourcePermanentSnapshot(sourceSnapshot);
        }
        if (ctx.entry() != null) {
            filterContext = filterContext.withXValue(ctx.entry().getXValue());
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
            case PlayerSubject.TriggeringPlayer ignored -> listOf(triggeringPlayer(ctx));
            case PlayerSubject.ActivePlayer ignored -> listOf(gameData.activePlayerId);
            case PlayerSubject.DefendingPlayer ignored -> listOf(ctx.entry().getDefendingPlayerId() != null
                    ? ctx.entry().getDefendingPlayerId()
                    : gameQueryService.defendingPlayerOfAttackTarget(gameData, ctx.entry().getAttackedTargetId()));
            case PlayerSubject.EnchantedPlayer ignored -> listOf(enchantedPlayer(ctx));
            case PlayerSubject.ChosenPlayer ignored -> listOf(chosenPlayer(ctx));
            case PlayerSubject.AcceptedPlayers ignored -> ctx.entry().getAcceptedPlayerIds().stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            case PlayerSubject.PlayersDealtDamage ignored -> ctx.entry().getPlayersDealtDamageThisResolution().stream()
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
            case PlayerSubject.Matching matching -> players(ctx, matching.players()).stream()
                    .filter(playerId -> targetLegalityService.matchesPlayerPredicate(gameData, ctx.controllerId(),
                            playerId, matching.filter(), ctx.sourceId(),
                            ctx.entry() == null ? null : ctx.entry().getTriggeringPlayerId()))
                    .toList();
        };
        return inApnapOrder(gameData, players);
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
