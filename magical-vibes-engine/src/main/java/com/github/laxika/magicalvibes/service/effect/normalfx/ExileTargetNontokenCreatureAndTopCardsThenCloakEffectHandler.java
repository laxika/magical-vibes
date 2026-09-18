package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNontokenCreatureAndTopCardsThenCloakEffect;
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

/** Resolves Become Anonymous' combined exile, shuffle, and cloak effect. */
@Component
@RequiredArgsConstructor
public class ExileTargetNontokenCreatureAndTopCardsThenCloakEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetNontokenCreatureAndTopCardsThenCloakEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (controllerId == null || target == null) {
            return;
        }

        List<CloakedCard> pile = new ArrayList<>();
        List<Card> targetCards = new ArrayList<>(target.cardsLeavingBattlefield());
        if (!permanentRemovalService.removePermanentToExileFaceDown(gameData, target)) {
            return;
        }
        addExiledCards(gameData, targetCards, pile);
        permanentRemovalService.removeOrphanedAuras(gameData);

        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library != null && !library.isEmpty()) {
            int count = Math.min(2, library.size());
            List<Card> topCards = new ArrayList<>(library.subList(0, count));
            library.subList(0, count).clear();
            for (Card card : topCards) {
                exileService.exileCardFaceDown(gameData, controllerId, card, null);
                addExiledCards(gameData, List.of(card), pile);
            }
        }

        Collections.shuffle(pile);
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<CloakedCard> enteredCards = new ArrayList<>();
        for (CloakedCard card : pile) {
            if (!gameData.removeFromExile(card.card().getId())) {
                continue;
            }
            Permanent cloaked = new Permanent(card.card());
            cloaked.setFaceDownAsCloaked();
            cloaked.tap();
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, card.ownerId(), cloaked, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(cloaked);
            enteredCards.add(card);
        }

        for (CloakedCard card : enteredCards) {
            battlefieldEntryService.processFaceDownCreatureETBTriggers(
                    gameData, card.ownerId(), card.card());
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " exiles and cloaks "
                        + enteredCards.size() + " card(s)."));
    }

    private void addExiledCards(GameData gameData, List<Card> cards, List<CloakedCard> pile) {
        for (Card card : cards) {
            ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
            if (exiled != null) {
                pile.add(new CloakedCard(exiled.card(), exiled.ownerId()));
            }
        }
    }

    private record CloakedCard(Card card, UUID ownerId) {
    }
}
