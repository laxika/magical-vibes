package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCreatureCardFromGraveyardOnTopAndDealPowerDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the combined library and power-damage effect used by Dead Reckoning. */
@Component
@RequiredArgsConstructor
public class PutTargetCreatureCardFromGraveyardOnTopAndDealPowerDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCreatureCardFromGraveyardOnTopAndDealPowerDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetCreatureCardFromGraveyardOnTopAndDealPowerDamageEffect) effect;

        List<UUID> graveyardTargets = entry.targetsForGroup(e.graveyardTargetGroup());
        if (graveyardTargets.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        UUID graveyardCardId = graveyardTargets.getFirst();
        Card graveyardCard = graveyard == null ? null : graveyard.stream()
                .filter(card -> card.getId().equals(graveyardCardId))
                .findFirst()
                .orElse(null);
        if (graveyardCard == null || !graveyardCard.hasType(CardType.CREATURE)) {
            return;
        }

        int power = graveyardCard.getPower() == null ? 0 : Math.max(0, graveyardCard.getPower());
        graveyard.remove(graveyardCard);
        graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, graveyardCard);
        graveyardReturnSupport.moveCardToDestination(gameData, controllerId, graveyardCard,
                GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY, null, null, false);

        if (power == 0) {
            return;
        }

        List<UUID> creatureTargets = entry.targetsForGroup(e.creatureTargetGroup());
        if (creatureTargets.isEmpty()) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureTargets.getFirst());
        if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        if (!damageSupport.isDamagePreventedForCreature(gameData, entry, creature)) {
            damageSupport.dealCreatureDamage(gameData, entry, creature, power);
        }
    }
}
