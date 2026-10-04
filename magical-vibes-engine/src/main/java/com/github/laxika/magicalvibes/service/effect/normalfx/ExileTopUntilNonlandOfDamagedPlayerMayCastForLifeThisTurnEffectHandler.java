package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithNormalCostEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Bismuth Mindrender's combat-damage trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null || !gameData.playerIds.contains(damagedPlayerId)) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        String damagedPlayerName = gameData.playerIdToName.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(damagedPlayerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card nonland = null;
        int exiledCount = 0;
        while (!library.isEmpty()) {
            Card top = library.removeFirst();
            exileService.exileCard(gameData, damagedPlayerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                nonland = top;
                break;
            }
        }

        if (nonland == null) {
            gameLogService.append(gameData, GameLog.text(
                    damagedPlayerName + " exiles " + exiledCount
                            + " card(s) from the top of their library — no nonland card found."));
            log.info("Game {} - {} dug entire library ({} cards) with no nonland for {}",
                    gameData.id, damagedPlayerName, exiledCount, entry.getCard().getName());
            return;
        }

        gameData.exilePlayPermissions.put(nonland.getId(), controllerId);
        gameData.exilePlayForLifeEqualToManaValue.add(nonland.getId());
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId,
                List.of(new MayCastExiledCardWithNormalCostEffect(UUID.randomUUID(), false)),
                "Cast " + nonland.getName() + " by paying life equal to its mana value?",
                nonland.getId(), null, entry.getSourcePermanentId()));

        gameLogService.append(gameData, GameLog.builder()
                .text(damagedPlayerName + " exiles cards until ").card(nonland)
                .text(" — " + gameData.playerIdToName.get(controllerId)
                        + " may cast it now by paying life equal to its mana value.")
                .build());
        log.info("Game {} - {} dug {} card(s) into {}; {} may cast it now by paying life",
                gameData.id, damagedPlayerName, exiledCount, nonland.getName(),
                gameData.playerIdToName.get(controllerId));
    }
}
