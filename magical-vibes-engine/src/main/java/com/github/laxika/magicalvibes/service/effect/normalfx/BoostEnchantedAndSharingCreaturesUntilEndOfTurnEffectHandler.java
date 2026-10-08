package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent currentEnchanted = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (currentEnchanted == null) {
            var lastKnown = entry.lastKnownPermanentCard(entry.getTargetId());
            if (lastKnown == null || !lastKnown.hasType(CardType.CREATURE)) {
                return;
            }
            currentEnchanted = new Permanent(lastKnown);
        } else if (!gameQueryService.isCreature(gameData, currentEnchanted)) {
            return;
        }
        Permanent enchanted = currentEnchanted;

        var boost = (BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect) effect;
        List<Permanent> toBoost = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (permanent.getId().equals(enchanted.getId())
                    || gameQueryService.shareCreatureType(gameData, enchanted, permanent))) {
                toBoost.add(permanent);
            }
        });

        for (Permanent permanent : toBoost) {
            permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
            permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
        }
    }
}
