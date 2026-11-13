package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinCopyTriggeringSpellOrDealDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FlipCoinCopyTriggeringSpellOrDealDamageEffectHandler implements NormalEffectHandlerBean, ChoosesCoinFlipResultsFirst {

    private final CoinFlipService coinFlipService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    /** "When you win the flip, copy that spell. You may choose new targets for the copy." */
    private static final List<CardEffect> WON_FLIP = List.of(new CopyTriggeringSpellEffect());

    /** "When you lose the flip, [this] deals damage equal to that spell's mana value to any target." */
    private static final List<CardEffect> LOST_FLIP =
            List.of(new DealDamageToAnyTargetEffect(new TargetSpellManaValue()));

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinCopyTriggeringSpellOrDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        StackEntry triggeringSpell = entry.getTriggeringSpellSnapshot() != null
                ? new StackEntry(entry.getTriggeringSpellSnapshot())
                : gameQueryService.findStackEntryByCardId(gameData, entry.getTriggeringCardId());
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

        if (wonFlip && result.isActualCoinFlip()) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
            triggeredAbilityStackService.putMade(gameData, coinFlipRecord(gameData, entry, WON_FLIP, triggeringSpell));
            return;
        }

        triggeredAbilityStackService.putMade(gameData, coinFlipRecord(gameData, entry, LOST_FLIP, triggeringSpell));
    }

    /**
     * The entry of the "when you win the flip" or "when you lose the flip" ability: whichever side the coin lands
     * on, it records what the resolving entry was about, so "that spell" is the spell that entry triggered for, as
     * it is on the stack or as it last existed there.
     */
    private static StackEntry coinFlipRecord(GameData gameData, StackEntry entry, List<CardEffect> effects,
                                             StackEntry triggeringSpell) {
        StackEntry record = ReflexiveAbilitySupport.record(gameData, entry, effects,
                entry.getCard().getName() + "'s coin-flip trigger");
        if (record.getTriggeringSpellSnapshot() == null) {
            record.setTriggeringSpell(triggeringSpell);
        }
        return record;
    }
}
