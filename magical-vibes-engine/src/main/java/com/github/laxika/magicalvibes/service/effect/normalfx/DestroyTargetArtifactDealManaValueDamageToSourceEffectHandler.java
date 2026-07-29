package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetArtifactDealManaValueDamageToSourceEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetArtifactDealManaValueDamageToSourceEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetArtifactDealManaValueDamageToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent artifact = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (artifact == null) {
            return;
        }

        // The mana value is read before destruction; the artifact permanent itself stays usable as
        // the last-known-information damage source afterwards (CR 608.2h).
        int manaValue = artifact.getCard().getManaValue();

        destructionSupport.tryDestroyAndLog(gameData, artifact, entry.getCard().getName(), false);

        // The damage is dealt even if the artifact survived (regeneration) or was already gone.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source != null && manaValue > 0) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
            damageSupport.dealCreatureDamage(gameData, entry, source, damage, artifact);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
