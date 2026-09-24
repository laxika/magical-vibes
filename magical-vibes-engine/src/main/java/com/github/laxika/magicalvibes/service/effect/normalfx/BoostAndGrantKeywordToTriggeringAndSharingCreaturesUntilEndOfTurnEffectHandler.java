package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect) effect;
        Permanent triggeringPermanent = entry.getTriggeringPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (triggeringPermanent == null || !gameQueryService.isCreature(gameData, triggeringPermanent)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        List<Permanent> affectedPermanents = new ArrayList<>(battlefield);
        if (affectedPermanents.stream().noneMatch(
                permanent -> permanent.getId().equals(triggeringPermanent.getId()))) {
            affectedPermanents.add(triggeringPermanent);
        }

        for (Permanent permanent : affectedPermanents) {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || (!permanent.getId().equals(triggeringPermanent.getId())
                    && !gameQueryService.shareCreatureType(gameData, triggeringPermanent, permanent))) {
                continue;
            }

            permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
            permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());

            Set<Keyword> grantableKeywords = boost.keywords().stream()
                    .filter(keyword -> !gameQueryService.cantHaveOrGainKeyword(gameData, permanent, keyword))
                    .collect(Collectors.toSet());
            if (grantableKeywords.isEmpty()) {
                continue;
            }

            permanent.getGrantedKeywords().addAll(grantableKeywords);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}
