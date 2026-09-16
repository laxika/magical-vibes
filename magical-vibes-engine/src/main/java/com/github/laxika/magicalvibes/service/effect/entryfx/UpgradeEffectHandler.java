package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UpgradeEffect;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpgradeEffectHandler implements EntryReplacementHandlerBean {

    private final UpgradeSupport upgradeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UpgradeEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect) {
        upgradeSupport.selectIfSingleArtifact(gameData, controllerId, enteringPermanent);
    }
}
