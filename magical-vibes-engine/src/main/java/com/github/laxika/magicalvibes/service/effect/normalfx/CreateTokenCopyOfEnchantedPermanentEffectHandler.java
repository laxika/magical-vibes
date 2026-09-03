package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEnchantedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEnchantedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfEnchantedPermanentEffect) effect;
        UUID auraPermanentId = copyEffect.auraPermanentId() != null
                ? copyEffect.auraPermanentId() : entry.getSourcePermanentId();
        Permanent aura = gameQueryService.findPermanentById(gameData, auraPermanentId);
        Permanent enchanted = null;

        if (aura != null && aura.getAttachedTo() != null) {
            enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        } else if (aura == null && copyEffect.auraPermanentId() == null) {
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
        if (copyEffect.amount() <= 0) {
            return;
        }

        Card sourceCard = enchanted.getCard();
        tokenCopySupport.createTokenCopies(gameData, entry,
                Collections.nCopies(copyEffect.amount(), sourceCard), enchanted,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
