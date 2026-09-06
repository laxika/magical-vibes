package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnAllCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolve(gameData, entry, (ReturnAllCardsExiledWithSourceEffect) effect, null);
    }

    void returnAllCardsExiledWithSourceExcept(GameData gameData, StackEntry entry,
                                              UUID excludedCardId) {
        resolve(gameData, entry, new ReturnAllCardsExiledWithSourceEffect(), excludedCardId);
    }

    private void resolve(GameData gameData, StackEntry entry,
                         ReturnAllCardsExiledWithSourceEffect returnEffect,
                         UUID excludedCardId) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        if (returnEffect.turnFaceUp()) {
            for (int i = 0; i < gameData.exiledCards.size(); i++) {
                ExiledCardEntry exiledEntry = gameData.exiledCards.get(i);
                if (sourcePermanentId.equals(exiledEntry.sourcePermanentId())
                        && exiledEntry.faceDown()) {
                    gameData.exiledCards.set(i, new ExiledCardEntry(exiledEntry.card(),
                            exiledEntry.ownerId(), exiledEntry.sourcePermanentId(), false,
                            exiledEntry.exilerId()));
                }
            }
        }

        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(e -> sourcePermanentId.equals(e.sourcePermanentId()))
                .filter(e -> excludedCardId == null
                        || !excludedCardId.equals(e.card().getId()))
                .filter(e -> returnEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        e.card(), returnEffect.filter(), entry.getCard().getId()))
                .toList();

        boolean underControllerControl = returnEffect.underControllerControl();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();

        for (ExiledCardEntry exiledEntry : toReturn) {
            Card card = exiledEntry.card();
            UUID newControllerId = underControllerControl ? entry.getControllerId() : exiledEntry.ownerId();
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }

            Permanent perm = new Permanent(card);
            perm.setEnteredFromExile(true);
            perm.getPersistentGrantedKeywords().addAll(returnEffect.grantedKeywords());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, newControllerId, perm,
                    enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(perm);

            gameLogService.append(gameData, GameLog.builder().card(card).text(" returns to the battlefield under " + gameData.playerIdToName.get(newControllerId) + "'s control.").build());
            log.info("Game {} - {} returns from exile via {} (put into graveyard from battlefield)",
                    gameData.id, card.getName(), entry.getCard().getName());
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, newControllerId, card, null, false);
        }
    }
}
