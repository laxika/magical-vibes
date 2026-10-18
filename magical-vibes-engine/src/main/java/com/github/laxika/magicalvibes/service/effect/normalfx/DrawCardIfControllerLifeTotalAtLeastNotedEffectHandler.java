package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfControllerLifeTotalAtLeastNotedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the noted-life-total upkeep draw and updates the note afterward. */
@Component
@RequiredArgsConstructor
public class DrawCardIfControllerLifeTotalAtLeastNotedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardIfControllerLifeTotalAtLeastNotedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent livePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        // The noted life total is read from last known information when the source has left the battlefield.
        Permanent source = livePermanent != null ? livePermanent : entry.getSourcePermanentSnapshot();
        if (source == null) {
            Permanent departed = gameData.departedPermanentSnapshots.get(entry.getCard().getId());
            if (departed != null && departed.getId().equals(entry.getSourcePermanentId())) {
                source = departed;
            }
        }
        if (source == null) {
            return;
        }

        int currentLife = gameData.getLife(entry.getControllerId());
        if (currentLife >= source.getChosenNumber()) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
        }
        if (livePermanent != null) {
            livePermanent.setChosenNumber(gameData.getLife(entry.getControllerId()));
        }
    }
}
