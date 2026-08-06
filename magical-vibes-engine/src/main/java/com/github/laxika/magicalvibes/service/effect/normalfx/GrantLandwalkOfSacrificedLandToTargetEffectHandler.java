package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLandwalkOfSacrificedLandToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Resolves {@link GrantLandwalkOfSacrificedLandToTargetEffect} (Excavator): map the land types of
 * the land sacrificed to pay the cost onto their landwalk keywords, then hand the resulting grant
 * to {@link GrantKeywordEffectHandler} so the until-end-of-turn grant goes through the usual
 * layer-6 floating-effect path.
 */
@Component
@RequiredArgsConstructor
public class GrantLandwalkOfSacrificedLandToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GrantKeywordEffectHandler grantKeywordEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantLandwalkOfSacrificedLandToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Set<Keyword> landwalks = landwalksOfSacrificedLand(gameData, entry);
        if (landwalks.isEmpty()) {
            return;
        }
        grantKeywordEffectHandler.resolve(gameData, entry,
                new GrantKeywordEffect(landwalks, GrantScope.TARGET));
    }

    private Set<Keyword> landwalksOfSacrificedLand(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card sacrificed = source != null ? source.getChosenCard() : null;
        Set<Keyword> landwalks = EnumSet.noneOf(Keyword.class);
        if (sacrificed == null) {
            return landwalks;
        }
        for (Map.Entry<Keyword, CardSubtype> landwalk : Keyword.LANDWALK_MAP.entrySet()) {
            if (sacrificed.getSubtypes().contains(landwalk.getValue())) {
                landwalks.add(landwalk.getKey());
            }
        }
        return landwalks;
    }
}
