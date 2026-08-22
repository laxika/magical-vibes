package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinCopyTriggeringSpellOrDealDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FlipCoinCopyTriggeringSpellOrDealDamageEffectHandler implements NormalEffectHandlerBean {

    private final CoinFlipService coinFlipService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinCopyTriggeringSpellOrDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        StackEntry triggeringSpell = gameQueryService.findStackEntryByCardId(
                gameData, entry.getTriggeringCardId());
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        boolean wonFlip = result.heads();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        gameLogService.append(gameData, GameLog.text(
                playerName + (wonFlip ? " wins" : " loses") + " the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + "."));

        if (triggeringSpell == null) {
            return;
        }

        StackEntry spellSnapshot = new StackEntry(triggeringSpell);
        int manaValue = triggeringSpell.getCard().getManaValue() + triggeringSpell.getXValue();
        if (wonFlip) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
            CardEffect copyEffect = new CopyControllerCastSpellEffect(
                    spellSnapshot, triggeringSpell.getControllerId());
            effectHandlerRegistry.getHandler(copyEffect).resolve(gameData, entry, copyEffect);
            return;
        }

        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                entry.getCard(),
                controllerId,
                new ArrayList<>(List.of(new DealDamageToAnyTargetEffect(manaValue))),
                false,
                null,
                0,
                entry.getSourcePermanentId()));
    }
}
