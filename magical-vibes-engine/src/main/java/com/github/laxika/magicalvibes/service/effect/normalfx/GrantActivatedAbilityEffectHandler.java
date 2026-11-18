package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantActivatedAbilityEffect) effect;
        if (e.scope() == GrantScope.OWN_CREATURES
                && Duration.WHILE_SOURCE_REMAINS.equals(e.duration())
                && entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null) {
                return;
            }
            PermanentPredicate scope = new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledBySourceControllerPredicate(),
                    new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
            ));
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantActivatedAbilityEffect(
                            grantedAbility(e.ability(), entry).withGrantSource(entry.getSourcePermanentId()),
                            GrantScope.TARGET,
                            e.filter(),
                            e.duration()),
                    null,
                    null,
                    scope,
                    e.duration(),
                    0
            ));
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" grants \"" + e.ability().getDescription()
                            + "\" to creatures you control for as long as it remains on the battlefield.")
                    .build());
            log.info("Game {} - {} grants activated ability to creatures you control while it remains",
                    gameData.id, entry.getCard().getName());
            return;
        }
        int count = 0;
        if (e.scope() == GrantScope.SELF) {
            UUID sourceId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getTargetId();
            Permanent source = sourceId == null ? null
                    : gameQueryService.findPermanentById(gameData, sourceId);
            if (source != null) {
                grantTo(gameData, entry, source, e);
                count++;
            }
        } else if (e.scope() == GrantScope.TARGET) {
            if ((Duration.WHILE_SOURCE_REMAINS.equals(e.duration())
                    || Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(e.duration()))
                    && (entry.getSourcePermanentId() == null
                    || gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) == null)) {
                return;
            }
            // "Target creature gains '[ability]' until end of turn" (e.g. Banishing Knack).
            // Bound to a target group; falls back to the single-target id.
            List<UUID> ids = new ArrayList<>();
            if (entry.getTargetId() != null) {
                ids.add(entry.getTargetId());
            }
            for (UUID id : entry.targetsForEffect(effect)) {
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            }
            for (UUID id : ids) {
                Permanent target = gameQueryService.findPermanentById(gameData, id);
                if (target == null) {
                    continue; // Partially resolves — skip removed targets
                }
                grantTo(gameData, entry, target, e);
                count++;
            }
        } else if (e.scope() == GrantScope.ENCHANTED_PERMANENT) {
            // The Genju cycle: the permanent the source Aura was attached to at activation (captured
            // as the entry's target), falling back to the live attachment. No creature check.
            Permanent aura = entry.getSourcePermanentId() == null ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            UUID enchantedId = entry.getTargetId() != null ? entry.getTargetId()
                    : aura == null ? null : aura.getAttachedTo();
            Permanent enchanted = enchantedId == null ? null
                    : gameQueryService.findPermanentById(gameData, enchantedId);
            if (enchanted != null) {
                grantTo(gameData, entry, enchanted, e);
                count++;
            }
        } else {
            boolean grantsToAllCreatures = e.scope() == GrantScope.ALL_CREATURES
                    || e.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF;
            List<Permanent> battlefield = grantsToAllCreatures
                    ? gameData.playerBattlefields.values().stream().flatMap(List::stream).toList()
                    : gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            // OWN_CREATURES means "other creatures you control" — the source is excluded.
            // ALL_OWN_CREATURES includes the source.
            boolean excludeSource = e.scope() == GrantScope.OWN_CREATURES
                    || e.scope() == GrantScope.ALL_CREATURES;
            boolean grantsToLands = e.scope() == GrantScope.OWN_LANDS;
            boolean grantsToAllPermanents = e.scope() == GrantScope.OWN_PERMANENTS;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (grantsToLands && !gameQueryService.isLand(gameData, permanent)) {
                        continue;
                    }
                    if (!grantsToLands && !grantsToAllPermanents
                            && !gameQueryService.isCreature(gameData, permanent)) {
                        continue;
                    }
                    if (excludeSource && permanent.getId().equals(entry.getSourcePermanentId())) {
                        continue;
                    }
                    if (e.filter() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                        continue;
                    }
                    grantTo(gameData, entry, permanent, e);
                    count++;
                }
            }
        }

        Duration duration = e.duration();
        String durationText = Duration.UNTIL_YOUR_NEXT_TURN.equals(duration) ? "until your next turn"
                : Duration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE.equals(duration)
                ? "until the source card is cast from exile"
                : Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(duration) || Duration.WHILE_SOURCE_REMAINS.equals(duration)
                ? "for as long as the source remains on the battlefield"
                : Duration.PERMANENT.equals(duration) || Duration.STATIC.equals(duration) ? "indefinitely"
                : "until end of turn";
        String recipientText = e.scope() == GrantScope.SELF ? "permanent(s)" :
                e.scope() == GrantScope.OWN_LANDS ? "land(s)" :
                e.scope() == GrantScope.OWN_PERMANENTS ? "permanent(s)" : "creature(s)";
        
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" grants \"" + e.ability().getDescription() + "\" to " + count + " " + recipientText + " " + durationText + ".").build());
        log.info("Game {} - {} grants activated ability to {} {} {}",
                gameData.id, entry.getCard().getName(), count, recipientText, durationText);
    }

    private static void grantTo(GameData gameData, StackEntry entry, Permanent permanent,
                                GrantActivatedAbilityEffect grant) {
        Duration duration = grant.duration();
        if (Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(duration)
                || Duration.WHILE_SOURCE_REMAINS.equals(duration)
                || Duration.PERMANENT.equals(duration)
                || Duration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE.equals(duration)
                // "Until your next turn" ends on the turn of the effect's controller, even if the
                // permanent has changed control since (Hydro-Man, Fluid Felon).
                || Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantActivatedAbilityEffect(
                            grantedAbility(grant.ability(), entry).withGrantSource(entry.getSourcePermanentId()),
                            GrantScope.TARGET,
                            grant.filter(),
                            duration,
                            grant.expirationCardId() != null
                                    ? grant.expirationCardId() : entry.getCard().getId()
                    ),
                    permanent.getId(),
                    null,
                    null,
                    duration,
                    0
            ));
            return;
        }
        // Until end of turn, the one other duration a resolving grant states.
        gameData.addEffectOnPermanent(entry, new GrantActivatedAbilityEffect(grantedAbility(grant.ability(), entry),
                GrantScope.TARGET, grant.filter(), Duration.UNTIL_END_OF_TURN), permanent.getId(),
                Duration.UNTIL_END_OF_TURN);
    }

    private static ActivatedAbility grantedAbility(ActivatedAbility ability, StackEntry entry) {
        ActivatedAbility granted = ability.withGrantingPlayer(entry.getControllerId());
        // CR 602.5c: a whole-game "activate only once" cap belongs to the ability as acquired from this
        // object, not to other identically worded abilities (a later casting grants a fresh one).
        return granted.getMaxActivationsPerGame() != null ? granted.withAcquisitionId(UUID.randomUUID()) : granted;
    }
}
