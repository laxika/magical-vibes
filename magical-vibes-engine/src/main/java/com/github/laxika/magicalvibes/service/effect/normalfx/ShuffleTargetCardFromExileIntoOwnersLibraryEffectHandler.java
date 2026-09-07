package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardFromExileIntoOwnersLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShuffleTargetCardFromExileIntoOwnersLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetCardFromExileIntoOwnersLibraryEffect.class;
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

        if (!gameData.removeFromExile(exiled.card().getId())) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in exile)."));
            return;
        }

        gameData.playerDecks.get(exiled.ownerId()).add(exiled.card());
        LibraryShuffleHelper.shuffleLibrary(gameData, exiled.ownerId());
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getDescription() + " shuffles ", exiled.card(),
                        " into its owner's library."));
    }
}
