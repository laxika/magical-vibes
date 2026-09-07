package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.FlipCoinOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinOnEnterEffectHandler implements EntryReplacementHandlerBean {

    private final CoinFlipService coinFlipService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinOnEnterEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect) {
        FlipCoinOnEnterEffect flipEffect = (FlipCoinOnEnterEffect) effect;
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        boolean heads = result.heads();
        String sourceName = enteringPermanent.getCard().getName();

        String flipLog = heads
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + ".";
        gameLogService.append(gameData, GameLog.text(flipLog));
        if (heads) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
        } else {
            triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, controllerId);
        }

        int power = heads ? flipEffect.headsPower() : flipEffect.tailsPower();
        int toughness = heads ? flipEffect.headsToughness() : flipEffect.tailsToughness();
        Set<Keyword> keywords = heads ? flipEffect.headsKeywords() : flipEffect.tailsKeywords();
        SetBasePowerToughnessEffect setter = new SetBasePowerToughnessEffect(
                power, toughness, GrantScope.SELF, EffectDuration.PERMANENT);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), sourceName, enteringPermanent.getId(), controllerId, setter,
                enteringPermanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        enteringPermanent.getPersistentGrantedKeywords().addAll(keywords);

        log.info("Game {} - {} enters with base power and toughness {}/{}", gameData.id,
                sourceName, power, toughness);
    }
}
