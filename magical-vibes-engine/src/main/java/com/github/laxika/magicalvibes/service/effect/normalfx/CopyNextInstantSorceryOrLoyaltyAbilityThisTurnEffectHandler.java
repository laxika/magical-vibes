package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Creates {@link CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect#delayedAbility()}, a delayed triggered ability of the resolving spell or ability that triggers
 * only the next time its event occurs this turn (CR 603.7b), its source and controller those of the resolving object
 * (CR 603.7d, 603.7e).
 */
@Slf4j
@Component
public class CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(), copyEffect.delayedAbility(), entry.getSourcePermanentId()));
        log.info("Game {} - {} will copy their next instant, sorcery, or loyalty ability this turn", gameData.id, entry.getControllerId());
    }
}
