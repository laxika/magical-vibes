package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
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

        tokenCopySupport.createTokenCopies(
                gameData, entry, List.of(enchanted.getCard()), enchanted,
                entry.getControllerId(), ((CreateTokenCopyOfEnchantedPermanentEffect) effect).copyEffect());
    }
}
