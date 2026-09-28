package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantActivatedAbilityToTriggeringCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Records a perpetual activated-ability grant on the landfall trigger's entering card. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantActivatedAbilityToTriggeringCardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantActivatedAbilityToTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

        Card card = gameQueryService.findCardById(gameData, cardId);
        if (card == null) {
            return;
        }

        ActivatedAbility ability = ((PerpetuallyGrantActivatedAbilityToTriggeringCardEffect) effect).ability();
        gameData.perpetualActivatedAbilities.compute(card.getId(), (ignored, existing) -> {
            List<ActivatedAbility> updated = new ArrayList<>(existing == null ? List.of() : existing);
            if (!updated.contains(ability)) {
                updated.add(ability);
            }
            return List.copyOf(updated);
        });

        Permanent permanent = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (permanent != null && !permanent.getPersistentGrantedActivatedAbilities().contains(ability)) {
            permanent.getPersistentGrantedActivatedAbilities().add(ability);
        }
    }
}
