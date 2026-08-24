package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyDrawnInstantOrSorceryAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles casting or declining God-Eternal Kefnet's reduced-cost copy. */
@Component
@RequiredArgsConstructor
public class CopyDrawnInstantOrSorceryAndMayCastCopyHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SpellCastingService spellCastingService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyDrawnInstantOrSorceryAndMayCastCopyEffect.class;
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

        if (EffectResolution.needsTarget(copy)) {
            boolean hasLegalTargets = copy.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, copy, player.getId())
                    : !exileCastTargetSupport.firstSlotCandidates(gameData, copy, player.getId()).isEmpty();
            if (!hasLegalTargets) {
                gameData.removeFromExile(copy.getId());
                gameLogService.append(gameData,
                        GameLog.textCardText("The copy of ", copy, " has no legal targets and ceases to exist."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        try {
            spellCastingService.paySpellManaCostFromNonHandZone(gameData, player.getId(), copy, 0, Zone.EXILE);
        } catch (IllegalStateException ex) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " cannot pay the reduced cost for the copy of ", copy, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, player.getId(), List.of(copy.getId()));
    }
}
