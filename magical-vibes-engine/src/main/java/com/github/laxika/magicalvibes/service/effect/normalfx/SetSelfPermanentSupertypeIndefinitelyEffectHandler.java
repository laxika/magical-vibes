package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfPermanentSupertypeIndefinitelyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetSelfPermanentSupertypeIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfPermanentSupertypeIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetSelfPermanentSupertypeIndefinitelyEffect change =
                (SetSelfPermanentSupertypeIndefinitelyEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (change.gained()) {
            source.getPersistentRemovedSupertypes().remove(change.supertype());
            source.getPersistentGrantedSupertypes().add(change.supertype());
        } else {
            source.getPersistentGrantedSupertypes().remove(change.supertype());
            source.getPersistentRemovedSupertypes().add(change.supertype());
        }
    }
}
