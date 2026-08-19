package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEnchantedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEnchantedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent enchanted = null;

        if (aura != null && aura.getAttachedTo() != null) {
            enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        } else if (aura == null) {
            Permanent auraSnapshot = entry.getSourcePermanentSnapshot();
            if (auraSnapshot != null && auraSnapshot.getAttachedTo() != null) {
                enchanted = gameQueryService.findPermanentById(gameData, auraSnapshot.getAttachedTo());
                if (enchanted == null) {
                    enchanted = entry.getAttachedPermanentSnapshot();
                }
            }
        }

        if (enchanted == null) {
            log.info("Game {} - Enchanted permanent is no longer available, no token created", gameData.id);
            return;
        }

        Card sourceCard = enchanted.getCard();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(
                    sourceCard, new CreateTokenCopyOfTargetPermanentEffect());
            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);
            entry.getCreatedPermanentIds().add(tokenPermanent.getId());

            gameLogService.append(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
            log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(),
                    entry.getCard().getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);
        }
    }
}
