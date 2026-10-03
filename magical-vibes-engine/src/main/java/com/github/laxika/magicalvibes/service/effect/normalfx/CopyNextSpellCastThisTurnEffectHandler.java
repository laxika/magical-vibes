package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyNextSpellCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextSpellCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CopyNextSpellCastThisTurnEffect) effect;
        if (copyEffect.spellFilter() == null && copyEffect.removedSupertypes().isEmpty()) {
            gameData.delayedActions.add(new com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger(
                    entry.getControllerId(), entry.getSourcePermanentId(), entry.getCard(), null,
                    java.util.List.of(new com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect(true)),
                    true, false));
            log.info("Game {} - {} will copy their next spell this turn", gameData.id, entry.getControllerId());
            return;
        }

        gameData.pendingNextFilteredSpellCopiesThisTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> new java.util.ArrayList<>())
                .add(copyEffect);
        log.info("Game {} - {} will copy their next matching spell this turn", gameData.id, entry.getControllerId());
    }
}
