package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyEnchantedInstantAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.stereotype.Component;

import java.util.List;

/** Handles Spellweaver Volute's choice to cast its instant copy. */
@Component
public class CopyEnchantedInstantAndMayCastCopyHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final InputCompletionService inputCompletionService;

    public CopyEnchantedInstantAndMayCastCopyHandler(GameLogService gameLogService,
                                                     ExileFreeCastQueueSupport exileFreeCastQueueSupport,
                                                     InputCompletionService inputCompletionService) {
        this.gameLogService = gameLogService;
        this.exileFreeCastQueueSupport = exileFreeCastQueueSupport;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyEnchantedInstantAndMayCastCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card copy = ability.sourceCard();
        if (!accepted) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " declines to cast the copy of ", copy, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        CopyEnchantedInstantAndMayCastCopyEffect effect = ability.effects().stream()
                .filter(CopyEnchantedInstantAndMayCastCopyEffect.class::isInstance)
                .map(CopyEnchantedInstantAndMayCastCopyEffect.class::cast)
                .findFirst()
                .orElseThrow();
        gameData.pendingSpellweaverVoluteReattachment =
                new com.github.laxika.magicalvibes.model.PendingSpellweaverVoluteReattachment(
                        copy.getId(), ability.sourcePermanentId(), effect.enchantedCardId(), player.getId());
        exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, player.getId(), List.of(copy.getId()));
    }
}
