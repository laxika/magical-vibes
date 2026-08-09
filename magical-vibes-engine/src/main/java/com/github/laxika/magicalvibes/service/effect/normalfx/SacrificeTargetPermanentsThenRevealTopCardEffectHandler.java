package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentsThenRevealTopCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeTargetPermanentsThenRevealTopCardEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetPermanentsThenRevealTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets.isEmpty() && entry.getTargetId() != null) {
            targets = List.of(entry.getTargetId());
        }

        Map<UUID, UUID> sacrificedTargetControllers = new LinkedHashMap<>();
        List<UUID> targetsToSacrifice = new ArrayList<>();
        for (UUID targetId : targets) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            UUID controllerId = target == null
                    ? null
                    : gameQueryService.findPermanentController(gameData, targetId);
            if (target == null || controllerId == null
                    || !gameQueryService.canEffectCauseSacrifice(gameData, controllerId, entry.getControllerId())) {
                continue;
            }
            sacrificedTargetControllers.put(targetId, controllerId);
            targetsToSacrifice.add(targetId);
        }

        destructionSupport.performSimultaneousSacrifice(gameData, targetsToSacrifice);

        for (UUID targetId : targets) {
            UUID controllerId = sacrificedTargetControllers.get(targetId);
            if (controllerId != null) {
                revealTopCard(gameData, entry, controllerId);
            }
        }
    }

    private void revealTopCard(GameData gameData, StackEntry entry, UUID controllerId) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + sourceName + ").")
                .build());

        if (!isPermanentCard(topCard)) {
            return;
        }

        library.removeFirst();
        Permanent permanent = new Permanent(topCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(topCard, playerName));

        if (topCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, topCard, null, false);
        }
        if (topCard.hasType(CardType.PLANESWALKER) && topCard.getLoyalty() != null) {
            permanent.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, topCard.getLoyalty());
            permanent.setSummoningSick(false);
        }

        log.info("Game {} - {} puts {} onto the battlefield via {}",
                gameData.id, playerName, topCard.getName(), sourceName);
    }

    private boolean isPermanentCard(Card card) {
        if (card.getType() != null && card.getType().isPermanentType()
                && card.getType() != CardType.KINDRED) {
            return true;
        }
        return card.getAdditionalTypes().stream()
                .anyMatch(type -> type.isPermanentType() && type != CardType.KINDRED);
    }
}
