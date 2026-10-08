package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.BattlefieldEntryLibraryRemainder;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentsThenEachPlayerRevealsUntilPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetPermanentsThenEachPlayerRevealsUntilPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentsThenEachPlayerRevealsUntilPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = new ArrayList<>(entry.targetsForEffect(effect));
        for (UUID targetId : targetIds) {
            var target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            permanentRemovalService.removePermanentToExile(gameData, target);
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        var revealEffect = new RevealUntilCardPredicateRestOnBottomRandomEffect(
                new CardIsPermanentPredicate(), LibrarySearchDestination.BATTLEFIELD);
        List<BattlefieldEntryCard> cards = new ArrayList<>();
        List<BattlefieldEntryLibraryRemainder> remainders = new ArrayList<>();
        List<UUID> playerOrder = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = playerOrder.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) java.util.Collections.rotate(playerOrder, -activeIndex);
        for (UUID playerId : playerOrder) {
            StackEntry playerEntry = new StackEntry(entry);
            playerEntry.setControllerId(playerId);
            revealHandler.collectIntoBattlefieldBatch(gameData, playerEntry, revealEffect, cards, remainders);
        }
        battlefieldEntryBatchSupport.begin(gameData, cards, remainders);
    }
}
