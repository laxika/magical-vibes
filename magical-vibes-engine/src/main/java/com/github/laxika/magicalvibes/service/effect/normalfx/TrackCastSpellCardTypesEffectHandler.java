package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TrackCastSpellCardTypesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Resolves {@link TrackCastSpellCardTypesEffect}: marks the triggering spell's tracked types on the source's tracker
 * list and queues the draws and the sacrifice its completion calls for.
 */
@Component
@RequiredArgsConstructor
public class TrackCastSpellCardTypesEffectHandler implements NormalEffectHandlerBean {
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TrackCastSpellCardTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var tracker = (TrackCastSpellCardTypesEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) return;
        if (!source.getCard().getId().equals(source.getTrackedSpellCardTypesSourceCardId())) {
            source.getTrackedSpellCardTypes().clear();
            source.setTrackedSpellCardTypesSourceCardId(source.getCard().getId());
        }
        // "That type" is the triggering spell's, as it was when the ability triggered.
        StackEntry spell = entry.getTriggeringSpellSnapshot() != null ? entry.getTriggeringSpellSnapshot()
                : CopySupport.triggeringSpell(gameData, entry);
        if (spell == null || spell.getCard() == null) return;
        boolean markedNewType = false;
        for (var type : tracker.trackedTypes()) {
            if (spell.getCard().hasType(type)) {
                markedNewType |= source.getTrackedSpellCardTypes().add(type);
            }
        }
        if (!markedNewType) return;
        List<CardEffect> followUps = source.getTrackedSpellCardTypes().containsAll(tracker.trackedTypes())
                ? List.of(new DrawCardEffect(), new SacrificeSelfEffect(), new DrawCardEffect())
                : List.of(new DrawCardEffect());
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUps);
    }
}
