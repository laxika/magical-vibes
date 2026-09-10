package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves The Tale of Tamiyo's repeating mill-and-draw chapters. */
@Component
@RequiredArgsConstructor
public class MillControllerTwoRepeatIfSharedCardTypeThenDrawEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        while (true) {
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, 2);
            if (!twoMilledCardsShareACardType(milled)) {
                return;
            }
            drawService.resolveDrawCard(gameData, controllerId);
        }
    }

    private boolean twoMilledCardsShareACardType(List<Card> milled) {
        if (milled == null || milled.size() < 2) {
            return false;
        }
        for (int i = 0; i < milled.size(); i++) {
            for (int j = i + 1; j < milled.size(); j++) {
                for (CardType type : CardType.values()) {
                    if (milled.get(i).hasType(type) && milled.get(j).hasType(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
