package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetSpellIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Futurist Spellthief's targeted spell duplicate. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetSpellIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetSpellIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(null);
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (targetSpell == null || targetSpell.getCard() == null) {
            return;
        }

        Card duplicate = targetSpell.getCard().createRuntimeCopyWithNewId();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        duplicate.freeze();
        gameData.perpetualAnyColorManaForCastCardIds.add(duplicate.getId());
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into your hand."));
    }
}
