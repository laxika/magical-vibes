package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyDrawnInstantOrSorceryAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Creates and parks the copy revealed by God-Eternal Kefnet. */
@Component
@RequiredArgsConstructor
public class CopyDrawnInstantOrSorceryAndMayCastCopyEffectHandler implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyDrawnInstantOrSorceryAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyDrawnInstantOrSorceryAndMayCastCopyEffect copyEffect =
                (CopyDrawnInstantOrSorceryAndMayCastCopyEffect) effect;
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(entry.getControllerId()) + " reveals ")
                .card(copyEffect.drawnCard())
                .text(" with ")
                .card(entry.getCard())
                .text(".")
                .build());
        Card copy = copySupport.createCopyCard(copyEffect.drawnCard());
        copy.addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new Fixed(2)));

        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                entry.getControllerId(),
                List.of(copyEffect),
                "Cast the copy of " + copy.getName() + " paying its mana cost reduced by {2}?",
                copy.getId()
        ));
    }
}
