package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextLoyaltyAbilityThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Creates {@link CopyNextLoyaltyAbilityThisTurnEffect#delayedAbility()}, a delayed triggered ability of the resolving spell or ability that triggers
 * only the next time its event occurs this turn (CR 603.7b), its source and controller those of the resolving object
 * (CR 603.7d, 603.7e).
 */
@Slf4j
@Component
public class CopyNextLoyaltyAbilityThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextLoyaltyAbilityThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CopyNextLoyaltyAbilityThisTurnEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(), copyEffect.delayedAbility(), entry.getSourcePermanentId()));
        log.info("Game {} - {} will copy their next loyalty ability this turn", gameData.id, entry.getControllerId());
    }
}
