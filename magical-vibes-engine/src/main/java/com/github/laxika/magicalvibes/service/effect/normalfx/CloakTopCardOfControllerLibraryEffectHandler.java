package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CloakTopCardOfControllerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves cloaking the top card of each named player's library, in APNAP order. Records {@link StepKind#MANIFEST}:
 * the cloaked permanents and their controllers ("then attach this Equipment to it", Cryptic Coat).
 */
@Component
@RequiredArgsConstructor
public class CloakTopCardOfControllerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CloakTopCardOfControllerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cloak = (CloakTopCardOfControllerLibraryEffect) effect;
        entry.recordStep(StepKind.MANIFEST, StepOutcome.NONE);
        List<UUID> cloakedIds = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, cloak, cloak.player())) {
            Permanent cloaked = cloakTopCard(gameData, playerId);
            if (cloaked != null && gameData.findControllerOf(cloaked) != null) {
                cloakedIds.add(cloaked.getId());
                controllers.add(playerId);
            }
        }
        entry.recordStep(StepKind.MANIFEST, StepOutcome.ofObjects(cloakedIds, controllers));
    }

    private Permanent cloakTopCard(GameData gameData, UUID controllerId) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return null;
        }

        Card topCard = library.removeFirst();
        Permanent cloaked = new Permanent(topCard);
        cloaked.setFaceDownAsCloaked();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, cloaked);
        battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, topCard);

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " cloaks the top card of their library."));
        return cloaked;
    }
}
