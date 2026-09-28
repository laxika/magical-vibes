package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardOfDamagedPlayerLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves manifesting the damaged player's top library card under the trigger controller's control. */
@Component
@RequiredArgsConstructor
public class ManifestTopCardOfDamagedPlayerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final ManifestService manifestService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestTopCardOfDamagedPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null || controllerId == null) {
            return;
        }

        manifestService.manifestTopCardOfLibraryUnderController(
                gameData, damagedPlayerId, controllerId, entry.getCard());
    }
}
