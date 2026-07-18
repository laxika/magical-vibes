package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantActivatedAbilityEffect) effect;
        int count = 0;
        if (e.scope() == GrantScope.TARGET) {
            // "Target creature gains '[ability]' until end of turn" (e.g. Banishing Knack).
            // Bound to a target group; falls back to the single-target id.
            List<UUID> ids = entry.targetsForEffect(effect);
            if (ids.isEmpty() && entry.getTargetId() != null) {
                ids = List.of(entry.getTargetId());
            }
            for (UUID id : ids) {
                Permanent target = gameQueryService.findPermanentById(gameData, id);
                if (target == null) {
                    continue; // Partially resolves — skip removed targets
                }
                grantTo(target, e.ability(), e.duration());
                count++;
            }
        } else {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            // OWN_CREATURES means "other creatures you control" — the source is excluded.
            // ALL_OWN_CREATURES includes the source.
            boolean excludeSource = e.scope() == GrantScope.OWN_CREATURES;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (!gameQueryService.isCreature(gameData, permanent)) {
                        continue;
                    }
                    if (excludeSource && permanent.getId().equals(entry.getSourcePermanentId())) {
                        continue;
                    }
                    if (e.filter() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                        continue;
                    }
                    grantTo(permanent, e.ability(), e.duration());
                    count++;
                }
            }
        }

        String durationText = e.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN
                ? "until your next turn" : "until end of turn";
        String logEntry = entry.getCard().getName() + " grants \"" + e.ability().getDescription()
                + "\" to " + count + " creature(s) " + durationText + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" grants \"" + e.ability().getDescription() + "\" to " + count + " creature(s) " + durationText + ".").build());
        log.info("Game {} - {} grants activated ability to {} creature(s) {}",
                gameData.id, entry.getCard().getName(), count, durationText);
    }

    private static void grantTo(Permanent permanent, com.github.laxika.magicalvibes.model.ActivatedAbility ability,
                                EffectDuration duration) {
        if (duration == EffectDuration.UNTIL_YOUR_NEXT_TURN) {
            permanent.getUntilNextTurnActivatedAbilities().add(ability);
        } else {
            permanent.getTemporaryActivatedAbilities().add(ability);
        }
    }
}
