package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutLandsFromHandToBattlefieldEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves "each player may put any number of land cards from their hand onto the battlefield"
 * (The Great Aurora). Players choose in APNAP order, driven one at a time through
 * {@link com.github.laxika.magicalvibes.model.PendingInteraction.PutLandsFromHandChoice} and applied
 * by {@link PutLandsFromHandSupport}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerMayPutLandsFromHandToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PutLandsFromHandSupport putLandsFromHandSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayPutLandsFromHandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        putLandsFromHandSupport.beginNextChoice(gameData, apnapOrder(gameData), entry.getCard().getName());
    }

    /** Seating order rotated so the active player is first (APNAP). */
    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
