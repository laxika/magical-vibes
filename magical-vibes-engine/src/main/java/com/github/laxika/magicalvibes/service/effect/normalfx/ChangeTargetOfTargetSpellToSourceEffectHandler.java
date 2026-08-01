package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeTargetOfTargetSpellToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TargetRedirectionSupport targetRedirectionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeTargetOfTargetSpellToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        if (!targetSpell.hasAnyTarget()) {
            
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " has no effect (", targetSpell.getCard(), " has no targets)."));
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no effect (source permanent no longer on the battlefield)."));
            return;
        }

        if (targetSpell.isSingleTarget()) {
            if (sourcePermanentId.equals(targetSpell.getTargetId())) {
                gameLogService.append(gameData,
                        GameLog.cardTextCard(targetSpell.getCard(), " already targets ", entry.getCard(), "."));
                return;
            }
            if (targetRedirectionSupport.isValidNewTargetForSpell(gameData, targetSpell, sourcePermanentId)) {
                targetSpell.setTargetId(sourcePermanentId);
                
                gameLogService.append(gameData, GameLog.cardTextCard(targetSpell.getCard(), "'s target is changed to ", entry.getCard(), "."));
            } else {
                
                gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " is not a legal target for ", targetSpell.getCard(), ". Target not changed."));
            }
        } else {
            
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " has no effect (", targetSpell.getCard(), " does not have a single target)."));
        }
    }
}
