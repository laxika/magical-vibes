package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** "This creature / this spell perpetually loses this ability": the perpetual grant of the source object's card. */
@Component
@RequiredArgsConstructor
public class RemovePerpetualTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemovePerpetualTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (RemovePerpetualTriggeredAbilityEffect) effect;
        UUID cardId = thisObjectsCardId(gameData, entry);
        if (cardId == null) {
            return;
        }
        gameData.removePerpetualAbilitiesIf(cardId, removal.triggeredAbilityTrigger(),
                removal.triggeredAbilityType()::isInstance);
    }

    /**
     * "This creature" / "this spell": the card of the ability's source while it is still that object, the permanent
     * or the spell on the stack. A card it became in another zone is a new object (CR 400.7) and keeps the ability.
     */
    private UUID thisObjectsCardId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null) {
                return null;
            }
            return (source.getOriginalCard() != null ? source.getOriginalCard() : source.getCard()).getId();
        }
        UUID cardId = entry.getCard() == null ? null : entry.getCard().getId();
        boolean stillASpell = cardId != null && gameData.stack.stream()
                .anyMatch(onStack -> !onStack.getEntryType().isAbility() && !onStack.isCopy()
                        && onStack.getCard() != null && cardId.equals(onStack.getCard().getId()));
        return stillASpell ? cardId : null;
    }
}
