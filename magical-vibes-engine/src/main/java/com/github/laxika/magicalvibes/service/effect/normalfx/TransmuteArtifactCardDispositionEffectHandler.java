package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransmuteArtifactCardDispositionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransmuteArtifactCardDispositionEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransmuteArtifactCardDispositionEffect.class;
    }

    /**
     * The found artifact card never leaves the library until its final destination is decided, so a
     * card that can't enter the battlefield from the library (Grafdigger's Cage) simply stays there.
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TransmuteArtifactCardDispositionEffect disposition =
                (TransmuteArtifactCardDispositionEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (controllerId == null || library == null) {
            return;
        }
        Card selected = library.stream()
                .filter(card -> card.getId().equals(disposition.cardId()))
                .findFirst().orElse(null);
        if (selected == null) {
            return;
        }
        if (disposition.toBattlefield()) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, selected, Zone.LIBRARY)) {
                return;
            }
            library.remove(selected);
            Permanent permanent = new Permanent(selected, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent,
                    battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), null);
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, selected);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(selected,
                    gameData.playerIdToName.get(controllerId)));
            return;
        }

        library.remove(selected);
        UUID ownerId = selected.getOwnerId() == null ? controllerId : selected.getOwnerId();
        graveyardService.addCardToGraveyard(gameData, ownerId, selected, Zone.LIBRARY);
        gameLogService.append(gameData, GameLog.cardThen(selected,
                " is put into its owner's graveyard."));
    }
}
