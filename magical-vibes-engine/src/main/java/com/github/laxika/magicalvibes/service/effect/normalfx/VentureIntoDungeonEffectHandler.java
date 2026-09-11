package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AdditionalDungeonRoomTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VentureIntoDungeonEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VentureIntoDungeonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var playerId = entry.getControllerId();
        if (gameData.playersWhoVenturedIntoDungeonThisTurn.contains(playerId)
                && gameQueryService.playerHasDungeonVentureRestriction(gameData, playerId)) {
            return;
        }
        gameData.playersWhoVenturedIntoDungeonThisTurn.add(playerId);
        DungeonProgress progress = gameData.playerDungeonProgress.get(playerId);
        if (progress == null) {
            startDungeon(gameData, entry, playerId);
        } else if (progress.isBottomRoom()) {
            gameData.recordCompletedDungeon(playerId, progress.dungeon());
            triggerCollectionService.checkDungeonCompletionTriggers(gameData, playerId);
            startDungeon(gameData, entry, playerId);
        } else {
            DungeonProgress advanced = progress.advance();
            gameData.playerDungeonProgress.put(playerId, advanced);
            queueRoomAbility(gameData, entry, advanced);
        }
    }

    private void startDungeon(GameData gameData, StackEntry entry, java.util.UUID playerId) {
        DungeonProgress progress = new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0);
        gameData.playerDungeonProgress.put(playerId, progress);
        queueRoomAbility(gameData, entry, progress);
    }

    private void queueRoomAbility(GameData gameData, StackEntry entry, DungeonProgress progress) {
        int previousCopies = gameData.beginTriggeredAbilityCopies(
                1 + countAdditionalDungeonRoomTriggers(gameData, entry.getControllerId()));
        try {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    entry.getControllerId(),
                    entry.getCard().getName() + "'s dungeon room ability",
                    roomEffects(progress),
                    0,
                    entry.getSourcePermanentId()));
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    private int countAdditionalDungeonRoomTriggers(GameData gameData, java.util.UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
                continue;
            }
            count += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(AdditionalDungeonRoomTriggerEffect.class::isInstance)
                    .count();
        }
        return count;
    }

    private List<CardEffect> roomEffects(DungeonProgress progress) {
        if (progress.dungeon() != Dungeon.LOST_MINE_OF_PHANDELVER) {
            return List.of();
        }
        return switch (progress.roomIndex()) {
            case 0 -> List.of(new ScryEffect(1));
            case 1 -> List.of(CreateTokenEffect.ofTreasureToken(1));
            case 2 -> List.of(
                    new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                    new GainLifeEffect(1));
            case 3 -> List.of(new DrawCardEffect(1), new CompleteDungeonEffect(progress.dungeon()));
            default -> throw new IllegalStateException("Room index is outside Lost Mine of Phandelver");
        };
    }
}
