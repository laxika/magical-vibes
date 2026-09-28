package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

/** Records perpetual keyword grants on the card that caused the surrounding trigger. */
@Component
public class PerpetuallyGrantKeywordsToTriggeringCardEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordsToTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

        var grant = (PerpetuallyGrantKeywordsToTriggeringCardEffect) effect;
        gameData.perpetualCardKeywords
                .computeIfAbsent(cardId, ignored -> EnumSet.noneOf(Keyword.class))
                .addAll(grant.keywords());
    }
}
