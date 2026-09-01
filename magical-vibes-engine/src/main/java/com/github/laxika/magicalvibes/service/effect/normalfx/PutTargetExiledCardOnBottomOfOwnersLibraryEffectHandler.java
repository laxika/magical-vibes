package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetExiledCardOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetExiledCardOnBottomOfOwnersLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetExiledCardOnBottomOfOwnersLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetZone() != Zone.EXILE || entry.getTargetId() == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (no valid exile target)."));
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(entry.getTargetId());
        if (exiled == null || exiled.faceDown()) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription()
                            + " fizzles (target is no longer a face-up card in exile)."));
            return;
        }

        var library = gameData.playerDecks.get(exiled.ownerId());
        if (library == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (owner has no library)."));
            return;
        }

        if (!gameData.removeFromExile(exiled.card().getId())) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in exile)."));
            return;
        }

        library.addLast(exiled.card());
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getDescription() + " puts ", exiled.card(),
                        " on the bottom of its owner's library."));
    }
}
