package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyDrawnInstantOrSorceryAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyDrawnInstantOrSorceryAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyDrawnInstantOrSorceryAndMayCastCopyEffect copyEffect =
                (CopyDrawnInstantOrSorceryAndMayCastCopyEffect) effect;
        Card drawnCard = copyEffect.drawnCard();
        if (!drawnCard.hasType(CardType.INSTANT) && !drawnCard.hasType(CardType.SORCERY)) {
            return;
        }
        Card copy = copySupport.createCopyCard(drawnCard);
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
