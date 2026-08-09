package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.CountersOnLinkedPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithSourceCounterPTEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class CreateTokenWithSourceCounterPTEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithSourceCounterPTEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var create = (CreateTokenWithSourceCounterPTEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        CountersOnLinkedPermanent linkedCounters =
                new CountersOnLinkedPermanent(create.counterType(), sourceId);
        CreateTokenEffect template = create.tokenTemplate();
        var tokenEffects = new EnumMap<EffectSlot, CardEffect>(EffectSlot.class);
        if (template.tokenEffects() != null) {
            tokenEffects.putAll(template.tokenEffects());
        }
        tokenEffects.put(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(linkedCounters, linkedCounters));

        var linkedTemplate = template.withTokenEffects(tokenEffects);
        var createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), linkedTemplate, entry.getCard().getSetCode());
        entry.getCreatedPermanentIds().addAll(createdIds);
        gameData.sourceCreatedTokens
                .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet())
                .addAll(createdIds);
    }
}
