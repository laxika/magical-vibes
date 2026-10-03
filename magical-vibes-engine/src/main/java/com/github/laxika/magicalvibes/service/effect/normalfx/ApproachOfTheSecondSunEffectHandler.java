package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ApproachOfTheSecondSunEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import lombok.RequiredArgsConstructor;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApproachOfTheSecondSunEffectHandler implements NormalEffectHandlerBean {

    /** "Seventh from the top" as a 0-based library index. */
    private static final int SEVENTH_FROM_TOP = 6;

    private final WinGameEffectHandler winGameEffectHandler;
    private final LifeSupport lifeSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ApproachOfTheSecondSunEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        boolean castFromHand = !entry.isCopy() && entry.getSourceZone() == Zone.HAND;
        // This cast is already recorded, so a count >= 2 means at least one *other* same-named spell
        // was cast earlier this game.
        int sameNameCasts = gameData.getSpellsCastThisGameByNameCount(controllerId, entry.getCard().getName());

        if (castFromHand && sameNameCasts >= 2) {
            winGameEffectHandler.resolve(gameData, entry, new WinGameEffect());
            return;
        }

        // Otherwise: put the spell into its owner's library seventh from the top and gain 7 life.
        if (!entry.isCopy()) {
            List<Card> library = gameData.playerDecks.get(entry.getOwnerId());
            int position = Math.min(SEVENTH_FROM_TOP, library.size());
            library.add(position, entry.getPhysicalCard());
            entry.setSpellMovedDuringResolution(true);
            triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, entry.getOwnerId(), 1);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " is put " + (position + 1) + " from the top of its owner's library."));
        }
        lifeSupport.applyGainLife(gameData, controllerId, 7, null, entry.getCard(), entry.getEntryType());
    }
}
