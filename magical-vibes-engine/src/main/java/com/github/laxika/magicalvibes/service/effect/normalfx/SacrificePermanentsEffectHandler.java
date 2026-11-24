package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves filtered sacrifices. The sacrificing players come from {@code SubjectResolutionService}.
 * A single affected player uses the appropriate creature or permanent selection; an "each player"
 * subject, or any subject naming several players, has every player choose in APNAP order and then
 * sacrifices everything chosen at once (CR 101.4).
 * Nothing changes before the first sacrifice (log lines about players with nothing to sacrifice
 * are written after it), so a competing death replacement can be chosen first.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificePermanentsEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentsEffect) effect;
        // "Sacrifice a creature" (bare creature filter) uses the single-select creature primitive;
        // every other filter uses the multi-permanent-choice flow. Both are behaviourally tested.
        boolean creatureSingleSac = e.filter() instanceof PermanentIsCreaturePredicate
                && !e.simultaneousChoices();

        List<UUID> players = subjectResolutionService.players(gameData, entry, e, e.player());
        // Each player's count is read before this step begins, so "sacrifices that many" reads the earlier
        // sacrifice step it refers to (CR 608.2c), not this one.
        Map<UUID, Integer> counts = new HashMap<>();
        for (UUID playerId : players) {
            counts.put(playerId, evaluateCount(gameData, entry, e, playerId));
        }
        // A new sacrifice step: every permanent sacrificed from here on, as this resolves or as its sacrifice prompts
        // are answered, is added to the step's departures with its last-known information
        // (DestructionSupport.sacrificeTogether), which make its SACRIFICE outcome.
        entry.beginSacrificeStep();
        if (e.upToCount()) {
            if (players.size() > 1) {
                throw new IllegalStateException("An up-to-count sacrifice names one player: " + e.player());
            }
            if (!players.isEmpty()) {
                resolveUpToCount(gameData, entry, e, players.getFirst(), counts.get(players.getFirst()));
            }
            return;
        }
        if (e.player() instanceof PlayerSubject.Each || e.player() instanceof PlayerSubject.Matching
                || players.size() > 1) {
            resolveSelectedPlayers(gameData, entry, e, players, creatureSingleSac, counts);
        } else if (!players.isEmpty()) {
            resolveSinglePlayer(gameData, entry, e, players.getFirst(), creatureSingleSac,
                    counts.get(players.getFirst()));
        }
    }

    void resolveForPlayer(GameData gameData, StackEntry entry, SacrificePermanentsEffect effect,
            UUID playerId) {
        resolveSinglePlayer(gameData, entry, effect, playerId,
                effect.filter() instanceof PermanentIsCreaturePredicate,
                evaluateCount(gameData, entry, effect, playerId));
    }

    public boolean hasLegalSacrificeChoice(GameData gameData, StackEntry entry,
            SacrificePermanentsEffect effect, UUID playerId) {
        int count = sacrificeCount(gameData, entry, effect, playerId);
        if (count <= 0) {
            return false;
        }
        int matchingCount = sacrificeCandidates(gameData, entry, effect, playerId).size();
        return effect.upToCount() ? matchingCount > 0 : matchingCount >= count;
    }

    /**
     * The permanents {@code playerId} could sacrifice for {@code effect}: those they control matching its filter that
     * can be sacrificed (CR 608.2d); none while the effect can't make them sacrifice.
     */
    public List<UUID> sacrificeCandidates(GameData gameData, StackEntry entry, SacrificePermanentsEffect effect,
            UUID playerId) {
        if (playerId == null || !gameData.playerIds.contains(playerId)
                || isSacrificeProtected(gameData, entry, playerId)) {
            return List.of();
        }
        FilterContext filterContext = filterContextFor(gameData, entry);
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(p, effect.filter(), filterContext))
                .filter(p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p, effect.filter()))
                .filter(p -> !gameQueryService.isCreatureTokenProtectedFromResolvingTriggeredAbility(gameData, p))
                .map(Permanent::getId)
                .toList();
    }

    /** How many permanents {@code effect} has {@code playerId} sacrifice. */
    public int sacrificeCount(GameData gameData, StackEntry entry, SacrificePermanentsEffect effect, UUID playerId) {
        return evaluateCount(gameData, entry, effect, playerId);
    }

    /**
     * "Sacrifice any number of …": the player chooses how many of their matching permanents to sacrifice, none
     * included, so the choice is asked even with one candidate. Its completion sacrifices the chosen ones together
     * through {@code DestructionSupport.sacrificeTogether}, which adds them to this SACRIFICE step.
     */
    private void resolveUpToCount(GameData gameData, StackEntry entry, SacrificePermanentsEffect e, UUID playerId,
            int count) {
        if (isSacrificeProtected(gameData, entry, playerId)) {
            return;
        }
        if (count <= 0) {
            return;
        }
        FilterContext filterContext = filterContextFor(gameData, entry);
        List<UUID> matchingIds = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))
                .filter(p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p, e.filter()))
                .filter(p -> !gameQueryService.isCreatureTokenProtectedFromResolvingTriggeredAbility(gameData, p))
                .map(Permanent::getId)
                .toList();
        String playerName = gameData.playerIdToName.get(playerId);
        if (matchingIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no matching permanents to sacrifice."));
            log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
            return;
        }
        int maximum = Math.min(count, matchingIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, playerId, matchingIds, maximum,
                new MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount(entry),
                count == Integer.MAX_VALUE
                        ? "Choose any number of permanents to sacrifice."
                        : "Choose up to " + maximum + " permanents to sacrifice.");
    }

    private void resolveSinglePlayer(GameData gameData, StackEntry entry, SacrificePermanentsEffect e,
            UUID playerId, boolean creatureSingleSac, int count) {
        if (isSacrificeProtected(gameData, entry, playerId)) {
            if (!creatureSingleSac && (e.recordSacrificedCount() || e.recordSacrificedPower())) {
                entry.setEventValue(0);
            }
            return;
        }

        if (creatureSingleSac) {
            destructionSupport.performSacrificeCreatureForPlayer(gameData, playerId);
            return;
        }

        if (count <= 0) {
            if (e.recordSacrificedCount() || e.recordSacrificedPower()) {
                entry.setEventValue(0);
            }
            // A dynamic count can evaluate to zero ("for each creature put into your graveyard this
            // turn" with no deaths) — nothing is sacrificed and no choice is prompted.
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            if (e.recordSacrificedCount() || e.recordSacrificedPower()) {
                entry.setEventValue(0);
            }
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " has no permanents to sacrifice.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no permanents to sacrifice", gameData.id, playerName);
            return;
        }

        FilterContext filterContext = filterContextFor(gameData, entry);
        List<Permanent> matching = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))
                .filter(p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p, e.filter()))
                .filter(p -> !gameQueryService.isCreatureTokenProtectedFromResolvingTriggeredAbility(gameData, p))
                .toList();

        if (matching.isEmpty()) {
            if (e.recordSacrificedCount() || e.recordSacrificedPower()) {
                entry.setEventValue(0);
            }
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " has no matching permanents to sacrifice.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
            return;
        }

        if (e.recordSacrificedPower()) {
            entry.setEventValue(matching.stream()
                    .mapToInt(p -> gameQueryService.getEffectivePower(gameData, p))
                    .max()
                    .orElse(0));
        }

        if (matching.size() <= count) {
            // Sacrifice all matching — no choice needed
            destructionSupport.performSimultaneousSacrifice(gameData,
                    matching.stream().map(Permanent::getId).toList());
            if (e.recordSacrificedCount()) {
                entry.setEventValue(matching.size());
            }
        } else {
            // More matching permanents than required — prompt player to choose
            List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
            playerInputService.beginMultiPermanentChoice(gameData, playerId, matchingIds, count,
                    new MultiPermanentChoiceContext.ForcedSacrifice(
                            playerId, List.of(), List.of(), false, e.recordSacrificedCount()),
                    "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to sacrifice.");
        }
    }

    private void resolveSelectedPlayers(GameData gameData, StackEntry entry, SacrificePermanentsEffect e,
            List<UUID> playerIds, boolean creatureSingleSac, Map<UUID, Integer> counts) {
        if (e.recordSacrificedCount()) {
            entry.setEventValue(0);
        }
        if (e.recordSacrificedPower()) {
            entry.setEventValue(0);
        }
        // A lone affected player has no simultaneity to preserve, so a creature-only sacrifice uses the
        // single-select creature primitive, as it does for every other single-player sacrifice.
        if (creatureSingleSac && playerIds.size() == 1) {
            resolveSinglePlayer(gameData, entry, e, playerIds.getFirst(), true, counts.get(playerIds.getFirst()));
            return;
        }

        // Per CR 101.4 and the Destructive Force ruling (2010-08-15): active player chooses first,
        // then each other player in turn order, then all chosen permanents are sacrificed at the
        // same time. Collect all IDs to sacrifice and defer actual sacrifice until all choices
        // are made.
        FilterContext filterContext = filterContextFor(gameData, entry);
        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        List<String> playersWithNothingToSacrifice = new ArrayList<>();

        for (UUID playerId : playerIds) {
            if (isSacrificeProtected(gameData, entry, playerId)) {
                continue;
            }

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            int count = counts.get(playerId);
            if (count <= 0) {
                continue;
            }

            List<Permanent> matching = battlefield.stream()
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))
                    .filter(p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p, e.filter()))
                    .filter(p -> !gameQueryService.isCreatureTokenProtectedFromResolvingTriggeredAbility(gameData, p))
                    .toList();

            if (matching.isEmpty()) {
                playersWithNothingToSacrifice.add(gameData.playerIdToName.get(playerId));
                continue;
            }

            if (matching.size() <= count && !e.simultaneousChoices()) {
                // No choice needed — mark all for simultaneous sacrifice
                matching.stream().map(Permanent::getId).forEach(autoSacrificeIds::add);
            } else {
                // Player must choose — add to queue
                List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                choosers.add(new PendingForcedSacrifice(playerId, Math.min(count, matchingIds.size()), matchingIds));
            }
        }

        if (e.simultaneousChoices()
                && choosers.stream().allMatch(choice -> choice.validPermanentIds().size() <= choice.count())) {
            choosers.forEach(choice -> autoSacrificeIds.addAll(choice.validPermanentIds()));
            choosers.clear();
        }
        if (choosers.isEmpty()) {
            // All players auto-resolved — sacrifice everything now
            if (e.recordSacrificedPower()) {
                entry.setEventValue(maxEffectivePower(gameData, autoSacrificeIds));
            }
            destructionSupport.performSimultaneousSacrifice(gameData, autoSacrificeIds);
            if (e.recordSacrificedCount()) {
                entry.setEventValue(autoSacrificeIds.size());
            }
        } else {
            // Some players need to choose — begin the first prompt
            destructionSupport.beginNextForcedSacrificeFromQueue(
                    gameData, choosers, autoSacrificeIds, e.simultaneousChoices(), null,
                    e.recordSacrificedCount(), e.recordSacrificedPower());
        }
        for (String playerName : playersWithNothingToSacrifice) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no matching permanents to sacrifice."));
            log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
        }
    }

    private int maxEffectivePower(GameData gameData, List<UUID> permanentIds) {
        return permanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                .max()
                .orElse(0);
    }

    /**
     * Returns {@code true} when this effect belongs to an opponent of {@code playerId} and that
     * player can't be caused to sacrifice permanents by opponents' spells and abilities (Sigarda,
     * Host of Herons). Such a player is simply skipped — no prompt, no log entry.
     */
    private boolean isSacrificeProtected(GameData gameData, StackEntry entry, UUID playerId) {
        return !gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId());
    }

    /**
     * Evaluates the sacrifice count. With {@code countPerSacrificingPlayer} the amount is evaluated
     * from {@code sacrificingPlayerId}'s perspective, so a {@code CountScope.CONTROLLER} amount
     * counts that player's own permanents ("for each white permanent they control").
     */
    private int evaluateCount(GameData gameData, StackEntry entry, SacrificePermanentsEffect e,
            UUID sacrificingPlayerId) {
        Permanent source = resolveSourcePermanent(gameData, entry);
        AmountContext context = AmountContext.forStackEntry(entry, source);
        if (e.countPerSacrificingPlayer()) {
            context = new AmountContext(sacrificingPlayerId, source, context.targetPermanentId(),
                    context.xValue(), context.eventValue(), context.chosenPermanentId());
        }
        return amountEvaluationService.evaluate(gameData, e.count(), context);
    }

    /**
     * Builds a filter context so source-relative predicates (e.g.
     * {@code PermanentNotPredicate(PermanentIsSourceCardPredicate)} for "other than this creature")
     * resolve correctly. Prefer the permanent's original (front-face) card id so DFC transforms
     * still exclude the source.
     * A {@code PermanentIsTargetCardPredicate} reads the card a rider was handed in the entry's target cards (Feast of
     * Worms' destroyed land) or, failing that, the entry's own target permanent (Hotel of Fears: "Put X +1/+1 counters
     * on target creature you control ... Then sacrifice another creature").
     */
    FilterContext filterContextFor(GameData gameData, StackEntry entry) {
        Permanent source = resolveSourcePermanent(gameData, entry);
        UUID sourceCardId = source != null && source.getOriginalCard() != null
                ? source.getOriginalCard().getId()
                : (entry.getCard() != null ? entry.getCard().getId() : null);
        UUID targetCardId = entry.getTargetCardIds() == null || entry.getTargetCardIds().isEmpty()
                ? targetPermanentCardId(gameData, entry)
                : entry.getTargetCardIds().getFirst();
        return FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(source)
                .withSourcePermanentId(source != null ? source.getId() : entry.getSourcePermanentId())
                .withTargetCardId(targetCardId)
                .withTriggeringPermanentId(entry.getTriggeringPermanentId())
                .withTriggeringPermanentSnapshot(entry.getTriggeringPermanentSnapshot());
    }

    private UUID targetPermanentCardId(GameData gameData, StackEntry entry) {
        Permanent target = entry.getTargetId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getTargetId())
                : null;
        return target != null ? target.getOriginalCard().getId() : null;
    }

    private Permanent resolveSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) {
            source = entry.getSacrificedPermanentSnapshot();
        }
        return source;
    }
}
