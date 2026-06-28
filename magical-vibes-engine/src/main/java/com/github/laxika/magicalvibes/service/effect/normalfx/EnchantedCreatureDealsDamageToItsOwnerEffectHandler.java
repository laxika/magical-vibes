package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToItsOwnerEffect;
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
public class EnchantedCreatureDealsDamageToItsOwnerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsDamageToItsOwnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedCreatureDealsDamageToItsOwnerEffect) effect;

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) return;

        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (creature == null) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return;

        UUID ownerId = gameData.stolenCreatures.getOrDefault(creature.getId(), controllerId);

        String creatureName = creature.getCard().getName();
        String ownerName = gameData.playerIdToName.get(ownerId);

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);

        // Create a temporary stack entry with the creature as source for correct damage attribution
        StackEntry creatureEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                creature.getCard(),
                controllerId,
                creatureName + " deals damage to its owner",
                List.of(),
                ownerId,
                creature.getId()
        );

        damageSupport.dealDamageToPlayer(gameData, creatureEntry, ownerId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
