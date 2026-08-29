package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndTopCardThenManifestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Jeskai Infiltrator's combat-damage trigger. */
@Component
@RequiredArgsConstructor
public class ExileSelfAndTopCardThenManifestEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAndTopCardThenManifestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        List<Card> cardsToManifest = new ArrayList<>();

        if (source == null) {
            manifestTopCard(gameData, entry, controllerId);
            return;
        }

        cardsToManifest.addAll(source.cardsLeavingBattlefield());
        if (!permanentRemovalService.removePermanentToExile(gameData, source, true)) {
            manifestTopCard(gameData, entry, controllerId);
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library != null && !library.isEmpty()) {
            Card topCard = library.removeFirst();
            exileService.exileCardFaceDown(gameData, controllerId, topCard, null);
            cardsToManifest.add(topCard);
        }

        Collections.shuffle(cardsToManifest);
        manifestCards(gameData, controllerId, cardsToManifest);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " exiles itself and manifests the exiled cards."));
    }

    private void manifestTopCard(GameData gameData, StackEntry entry, UUID controllerId) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " cannot manifest because its controller's library is empty."));
            return;
        }

        manifestCards(gameData, controllerId, List.of(library.removeFirst()));
    }

    private void manifestCards(GameData gameData, UUID controllerId, List<Card> cards) {
        for (Card card : cards) {
            gameData.removeFromExile(card.getId());
            Permanent manifested = new Permanent(card);
            manifested.setManifested(true);
            manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, manifested);
            battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, card);
        }
    }
}
