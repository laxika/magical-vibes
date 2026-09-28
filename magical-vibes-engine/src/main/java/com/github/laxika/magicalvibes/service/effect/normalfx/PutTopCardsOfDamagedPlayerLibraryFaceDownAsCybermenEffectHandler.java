package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Cybership's combat-damage trigger. */
@Component
@RequiredArgsConstructor
public class PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cybermanEffect = (PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffect) effect;
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null || controllerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        if (library == null || library.isEmpty() || cybermanEffect.count() == 0) {
            return;
        }

        int count = Math.min(cybermanEffect.count(), library.size());
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Card card = library.removeFirst();
            Permanent cyberman = new Permanent(card);
            cyberman.setFaceDown(2, 2,
                    Set.of(CardType.ARTIFACT, CardType.CREATURE),
                    Set.of(CardSubtype.CYBERMAN));
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, cyberman, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(cyberman);
            if (!controllerId.equals(damagedPlayerId)) {
                graveyardReturnSupport.trackStolenCreature(
                        gameData, cyberman.getId(), controllerId, damagedPlayerId);
            }
            battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, card);
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " puts " + count
                        + " card" + (count == 1 ? "" : "s")
                        + " onto the battlefield face down as Cybermen."));
    }
}
