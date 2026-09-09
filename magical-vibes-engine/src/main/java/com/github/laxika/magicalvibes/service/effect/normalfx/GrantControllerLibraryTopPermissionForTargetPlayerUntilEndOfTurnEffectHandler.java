package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect;
import org.springframework.stereotype.Component;

/** Resolves the temporary permission to use the target player's library top. */
@Component
public class GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }
        gameData.libraryTopCardPermissionsUntilEndOfTurn.add(
                new GameData.LibraryTopCardPermission(entry.getControllerId(), entry.getTargetId()));
    }
}
