package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastCardsExiledWithSourceUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastCardsExiledWithSourceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AllowCastCardsExiledWithSourceUntilEndOfTurnEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        List<ExiledCardEntry> matchingEntries;
        if (e.targetSpecificCard()) {
            ExiledCardEntry targetEntry = entry.getTargetId() == null
                    ? null : gameData.findExiledCard(entry.getTargetId());
            matchingEntries = targetEntry != null
                    && sourcePermanentId.equals(targetEntry.sourcePermanentId())
                    && (!e.ownOnly() || entry.getControllerId().equals(targetEntry.ownerId()))
                    && (e.filter() == null
                    || predicateEvaluationService.matchesCardPredicate(targetEntry.card(), e.filter(), null))
                    ? List.of(targetEntry) : List.of();
        } else {
            matchingEntries = gameData.getExiledWithPermanentEntries(sourcePermanentId, entry.getCard().getId()).stream()
                    .filter(exiledEntry -> !e.ownOnly()
                            || entry.getControllerId().equals(exiledEntry.ownerId()))
                    .filter(exiledEntry -> e.filter() == null
                            || predicateEvaluationService.matchesCardPredicate(exiledEntry.card(), e.filter(), null))
                    .toList();
        }
        if (matchingEntries.isEmpty()) return;

        UUID grantId = UUID.randomUUID();
        for (ExiledCardEntry matchingEntry : matchingEntries) {
            gameData.exileCastPermissionsUntilEndOfTurn.add(new GameData.ExileCastPermission(
                    grantId, sourcePermanentId, entry.getControllerId(), matchingEntry.card().getId(),
                    e.withoutPayingManaCost(), e.putOnBottomOfOwnersLibrary()));
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " may cast a card exiled with " + entry.getCard().getName()
                + " until end of turn."));
        log.info("Game {} - {} may cast one of {} card(s) exiled with {} until end of turn",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), matchingEntries.size(),
                entry.getCard().getName());
    }
}
