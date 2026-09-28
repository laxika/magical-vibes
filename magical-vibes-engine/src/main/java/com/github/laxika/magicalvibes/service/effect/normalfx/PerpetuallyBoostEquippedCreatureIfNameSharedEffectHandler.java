package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostEquippedCreatureIfNameSharedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Mace of Disruption's conditional perpetual attack boost. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostEquippedCreatureIfNameSharedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostEquippedCreatureIfNameSharedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equippedCreature = findEquippedCreature(gameData, entry);
        if (equippedCreature == null || !gameQueryService.isCreature(gameData, equippedCreature)) {
            return;
        }

        String creatureName = gameQueryService.getEffectiveName(gameData, equippedCreature);
        UUID controllerId = entry.getControllerId();
        if (creatureName == null || controllerId == null || !hasNameMatch(gameData, equippedCreature,
                creatureName, controllerId)) {
            return;
        }

        var boost = (PerpetuallyBoostEquippedCreatureIfNameSharedEffect) effect;
        gameData.perpetualCardPowerToughnessModifiers.merge(
                equippedCreature.getCard().getId(),
                new CardPowerToughnessModifier(boost.powerBoost(), boost.toughnessBoost()),
                (oldValue, newValue) -> new CardPowerToughnessModifier(
                        oldValue.power() + newValue.power(), oldValue.toughness() + newValue.toughness()));
    }

    private Permanent findEquippedCreature(GameData gameData, StackEntry entry) {
        UUID triggeringPermanentId = entry.getTriggeringPermanentId();
        if (triggeringPermanentId != null) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (triggeringPermanentId.equals(permanent.getId())) {
                        return permanent;
                    }
                }
            }
        }

        Permanent equipment = null;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (sourcePermanentId.equals(permanent.getId())) {
                        equipment = permanent;
                        break;
                    }
                }
                if (equipment != null) {
                    break;
                }
            }
        }
        if (equipment == null || equipment.getAttachedTo() == null) {
            return null;
        }

        UUID attachedTo = equipment.getAttachedTo();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (attachedTo.equals(permanent.getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private boolean hasNameMatch(GameData gameData, Permanent equippedCreature, String creatureName,
                                 UUID controllerId) {
        if (gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .anyMatch(permanent -> !permanent.getId().equals(equippedCreature.getId())
                        && gameQueryService.isCreature(gameData, permanent)
                        && creatureName.equals(gameQueryService.getEffectiveName(gameData, permanent)))) {
            return true;
        }

        return gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getName)
                .anyMatch(creatureName::equals);
    }
}
