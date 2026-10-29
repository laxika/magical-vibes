package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
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
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService permanentCopierService;

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
        UUID enchantedId = null;

        if (aura != null && aura.getAttachedTo() != null) {
            enchantedId = aura.getAttachedTo();
        } else if (aura == null && copyEffect.auraPermanentId() == null) {
            Permanent auraSnapshot = entry.getSourcePermanentSnapshot();
            if (auraSnapshot != null) {
                enchantedId = auraSnapshot.getAttachedTo();
            }
        }
        Permanent enchanted = enchantedId == null
                ? null : gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchanted == null && enchantedId != null) {
            UUID departedId = enchantedId;
            enchanted = gameData.departedPermanentSnapshots.values().stream()
                    .filter(departed -> departedId.equals(departed.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (enchanted == null && enchantedId == null && aura == null && copyEffect.useTriggeringCreatureLastKnown()) {
            // The Aura and its creature both left before resolution: use the creature as it last existed.
            enchanted = entry.getAttachedPermanentSnapshot();
        }

        if (enchanted == null) {
            log.info("Game {} - Enchanted permanent is no longer available, no token created", gameData.id);
            return;
        }
        if (copyEffect.amount() <= 0) {
            return;
        }

        Card sourceCard = permanentCopierService.copiableCard(enchanted);
        tokenCopySupport.createTokenCopies(gameData, entry,
                Collections.nCopies(copyEffect.amount(), sourceCard), enchanted,
                copyEffect.copyEffect());
    }
}
