package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Records a triggered ability on each creature currently controlled by the source's controller. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var perpetual = (PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of());
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || permanent.getOriginalCard() == null) {
                continue;
            }

            UUID cardId = permanent.getOriginalCard().getId();
            gameData.grantPerpetualAbilityOnce(cardId, perpetual.triggeredAbilitySlot(), perpetual.triggeredAbility());

            if (!permanent.getPersistentTriggeredEffects(perpetual.triggeredAbilitySlot())
                    .contains(perpetual.triggeredAbility())) {
                permanent.addPersistentTriggeredEffect(
                        perpetual.triggeredAbilitySlot(), perpetual.triggeredAbility());
            }
        }
    }
}
