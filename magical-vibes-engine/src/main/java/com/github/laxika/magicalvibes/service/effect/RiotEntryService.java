package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiotEntryService {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    public void applyIfPresent(GameData gameData, UUID controllerId, Permanent permanent) {
        int riotInstances = countRiotInstances(gameData, permanent);
        if (riotInstances == 0) {
            return;
        }

        for (int i = 0; i < riotInstances; i++) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    permanent.getCard(),
                    controllerId,
                    List.of(new RiotEffect()),
                    permanent.getCard().getName() + " - Riot: have it enter with a +1/+1 counter or haste?",
                    null,
                    null,
                    permanent.getId()));
        }
        playerInputService.processNextMayAbility(gameData);
    }

    private int countRiotInstances(GameData gameData, Permanent permanent) {
        if (!gameQueryService.hasKeyword(gameData, permanent, Keyword.RIOT)) {
            return 0;
        }

        int count = permanent.getCard().getKeywords().contains(Keyword.RIOT) ? 1 : 0;
        for (Permanent source : allPermanents(gameData)) {
            if (gameQueryService.computeStaticBonus(gameData, source).losesAllAbilities()) {
                continue;
            }
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantKeywordEffect grant
                        && grant.keywords().contains(Keyword.RIOT)
                        && appliesToTarget(gameData, source, permanent, grant)) {
                    count++;
                }
            }
        }

        for (FloatingContinuousEffect floating : gameData.floatingEffects) {
            if (!(floating.effect() instanceof GrantKeywordEffect grant)
                    || !grant.keywords().contains(Keyword.RIOT)
                    || !permanent.getId().equals(floating.affectedPermanentId())) {
                continue;
            }
            count++;
        }

        return Math.max(count, 1);
    }

    private boolean appliesToTarget(GameData gameData, Permanent source, Permanent target,
                                    GrantKeywordEffect grant) {
        GrantScope scope = grant.scope();
        UUID sourceControllerId = gameData.findControllerOf(source);
        UUID targetControllerId = gameData.findControllerOf(target);
        boolean sameController = sourceControllerId != null && sourceControllerId.equals(targetControllerId);
        boolean creature = gameQueryService.isCreature(gameData, target);
        boolean sourceExcluded = source.getId().equals(target.getId())
                && (scope == GrantScope.OWN_CREATURES || scope == GrantScope.ALL_CREATURES);
        boolean scopeMatches = switch (scope) {
            case OWN_CREATURES, ALL_OWN_CREATURES -> sameController && creature && !sourceExcluded;
            case OPPONENT_CREATURES -> !sameController && creature;
            case ALL_CREATURES, ALL_CREATURES_INCLUDING_SELF -> creature && !sourceExcluded;
            case OWN_PERMANENTS -> sameController && !sourceExcluded;
            case ALL_PERMANENTS -> !sourceExcluded;
            default -> false;
        };
        return scopeMatches && (grant.filter() == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, target, grant.filter()));
    }

    private List<Permanent> allPermanents(GameData gameData) {
        return gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream())
                .toList();
    }
}
