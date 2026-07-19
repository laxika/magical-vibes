package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the pushed half of Quenchable Fire's delayed trigger: prompts the affected party
 * ({@code entry.controllerId}, the paying player) with a "you may pay {cost}; if you don't, take N
 * damage" choice. The originally-targeted player or planeswalker is carried on the pending ability's
 * {@code targetCardId} so {@link DamageTargetPlayerOrPlaneswalkerUnlessPaysHandler} can route the
 * fallback damage to it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageTargetPlayerOrPlaneswalkerUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID payerId = entry.getControllerId();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(e),
                entry.getCard().getName() + " - Pay " + e.manaCost() + "?",
                targetId, e.manaCost(), null));
    }
}
