package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect) effect;

        UUID controllerId = entry.getTargetId();
        if (controllerId == null) return;

        Card sourceCard = entry.getDamageSourceCard();
        if (sourceCard == null) {
            Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (aura == null || !aura.isAttached()) return;

            Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
            if (creature == null) return;

            sourceCard = creature.getCard();
            controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
            if (controllerId == null) return;
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue(), entry);
        if (rawDamage <= 0) return;

        String creatureName = sourceCard.getName();

        StackEntry creatureEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                creatureName + " deals damage to its controller",
                List.of(),
                controllerId,
                entry.getSourcePermanentId()
        );

        damageSupport.dealDamageToPlayer(gameData, creatureEntry, controllerId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
