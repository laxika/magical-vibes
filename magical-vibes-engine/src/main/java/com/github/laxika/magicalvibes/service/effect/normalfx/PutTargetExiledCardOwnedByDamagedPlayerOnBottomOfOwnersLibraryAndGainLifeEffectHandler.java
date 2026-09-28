package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Time Reaper's damaged-player exile-card trigger. */
@Component
@RequiredArgsConstructor
public class PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetZone() != Zone.EXILE || entry.getTargetId() == null) {
            fizzle(gameData, entry, "no valid exile target");
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(entry.getTargetId());
        if (exiled == null || exiled.faceDown()) {
            fizzle(gameData, entry, "target is no longer a face-up card in exile");
            return;
        }

        if (entry.getAttackedTargetId() == null
                || !entry.getAttackedTargetId().equals(exiled.ownerId())) {
            fizzle(gameData, entry, "target is not owned by the damaged player");
            return;
        }

        var library = gameData.playerDecks.get(exiled.ownerId());
        if (library == null) {
            fizzle(gameData, entry, "owner has no library");
            return;
        }

        if (!gameData.removeFromExile(exiled.card().getId())) {
            fizzle(gameData, entry, "target is no longer in exile");
            return;
        }

        library.addLast(exiled.card());
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getDescription() + " puts ", exiled.card(),
                        " on the bottom of its owner's library."));
        lifeSupport.applyGainLife(gameData, entry.getControllerId(),
                ((PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect) effect).lifeGain(),
                entry.getCard().getName(), entry.getCard(), entry.getEntryType());
    }

    private void fizzle(GameData gameData, StackEntry entry, String reason) {
        gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (" + reason + ")."));
    }
}
