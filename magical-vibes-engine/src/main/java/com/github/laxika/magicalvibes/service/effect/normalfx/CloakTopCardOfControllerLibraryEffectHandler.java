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
    private final GraveyardReturnSupport graveyardReturnSupport;

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
        UUID libraryOwnerId = cloak.libraryOwner() == null
                ? null : subjectResolutionService.player(gameData, entry, cloak, cloak.libraryOwner());
        if (cloak.libraryOwner() != null && libraryOwnerId == null) {
            return;
        }
        for (UUID playerId : subjectResolutionService.players(gameData, entry, cloak, cloak.player())) {
            Permanent cloaked = cloakTopCard(gameData, libraryOwnerId != null ? libraryOwnerId : playerId, playerId);
            if (cloaked != null && gameData.findControllerOf(cloaked) != null) {
                cloakedIds.add(cloaked.getId());
                controllers.add(playerId);
            }
        }
        entry.recordStep(StepKind.MANIFEST, StepOutcome.ofObjects(cloakedIds, controllers));
    }

    /**
     * {@code controllerId} cloaks the top card of {@code libraryOwnerId}'s library: it enters face down as a 2/2 with
     * ward {2} under the cloaking player's control, which holds as a control effect when the card came from another
     * player's library.
     */
    private Permanent cloakTopCard(GameData gameData, UUID libraryOwnerId, UUID controllerId) {
        List<Card> library = gameData.playerDecks.get(libraryOwnerId);
        if (library == null || library.isEmpty()) {
            return null;
        }

        Card topCard = library.removeFirst();
        Permanent cloaked = new Permanent(topCard);
        cloaked.setFaceDownAsCloaked();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, cloaked);
        boolean ownLibrary = libraryOwnerId.equals(controllerId);
        if (!ownLibrary) {
            graveyardReturnSupport.trackStolenCreature(gameData, cloaked.getId(), controllerId, libraryOwnerId);
        }
        battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, topCard);

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " cloaks the top card of "
                        + (ownLibrary ? "their library." : gameData.playerIdToName.get(libraryOwnerId) + "'s library.")));
        return cloaked;
    }
}
