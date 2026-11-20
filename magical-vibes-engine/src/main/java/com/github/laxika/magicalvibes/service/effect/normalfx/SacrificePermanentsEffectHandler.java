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
import java.util.List;
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
        // A new sacrifice step: every permanent sacrificed from here on, as this resolves or as its sacrifice prompts
        // are answered, is added to the step's departures with its last-known information
        // (DestructionSupport.sacrificeTogether), which make its SACRIFICE outcome.
        entry.beginSacrificeStep();
        var e = (SacrificePermanentsEffect) effect;
        // "Sacrifice a creature" (bare creature filter) uses the single-select creature primitive;
        // every other filter uses the multi-permanent-choice flow. Both are behaviourally tested.
        boolean creatureSingleSac = e.filter() instanceof PermanentIsCreaturePredicate
                && !e.simultaneousChoices();

        List<UUID> players = subjectResolutionService.players(gameData, entry, e, e.player());
        if (e.player() instanceof PlayerSubject.Each || e.player() instanceof PlayerSubject.Matching
                || players.size() > 1) {
            resolveSelectedPlayers(gameData, entry, e, players, creatureSingleSac);
        } else if (!players.isEmpty()) {
            resolveSinglePlayer(gameData, entry, e, players.getFirst(), creatureSingleSac);
        }
    }

    void resolveForPlayer(GameData gameData, StackEntry entry, SacrificePermanentsEffect effect,
            UUID playerId) {
        resolveSinglePlayer(gameData, entry, effect, playerId,
                effect.filter() instanceof PermanentIsCreaturePredicate);
    }

    public boolean hasLegalSacrificeChoice(GameData gameData, StackEntry entry,
            SacrificePermanentsEffect effect, UUID playerId) {
        if (playerId == null || !gameData.playerIds.contains(playerId)
                || isSacrificeProtected(gameData, entry, playerId)) {
            return false;
        }

        int count = evaluateCount(gameData, entry, effect, playerId);
        if (count <= 0) {
            return false;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return false;
        }

        FilterContext filterContext = filterContextFor(gameData, entry);
        long matchingCount = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(p, effect.filter(), filterContext))
                .filter(p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p, effect.filter()))
                .filter(p -> !gameQueryService.isCreatureTokenProtectedFromResolvingTriggeredAbility(gameData, p))
                .count();
        return matchingCount >= count;
    }

    private void resolveSinglePlayer(GameData gameData, StackEntry entry, SacrificePermanentsEffect e,
            UUID playerId, boolean creatureSingleSac) {
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

        int count = evaluateCount(gameData, entry, e, playerId);
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
            List<UUID> playerIds, boolean creatureSingleSac) {
        if (e.recordSacrificedCount()) {
            entry.setEventValue(0);
        }
        if (e.recordSacrificedPower()) {
            entry.setEventValue(0);
        }
        // A lone affected player has no simultaneity to preserve, so a creature-only sacrifice uses the
        // single-select creature primitive, as it does for every other single-player sacrifice.
        if (creatureSingleSac && playerIds.size() == 1) {
            resolveSinglePlayer(gameData, entry, e, playerIds.getFirst(), true);
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

            int count = evaluateCount(gameData, entry, e, playerId);
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
     */
    private FilterContext filterContextFor(GameData gameData, StackEntry entry) {
        Permanent source = resolveSourcePermanent(gameData, entry);
        UUID sourceCardId = source != null && source.getOriginalCard() != null
                ? source.getOriginalCard().getId()
                : (entry.getCard() != null ? entry.getCard().getId() : null);
        UUID targetCardId = entry.getTargetCardIds() == null || entry.getTargetCardIds().isEmpty()
                ? null
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
