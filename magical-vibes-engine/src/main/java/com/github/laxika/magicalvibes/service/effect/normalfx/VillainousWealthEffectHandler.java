package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.VillainousWealthEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VillainousWealthEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VillainousWealthEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        List<UUID> exiledThisProcess = new ArrayList<>();
        int x = entry.getXValue();
        if (library != null) {
            for (int i = 0; i < x && !library.isEmpty(); i++) {
                Card card = library.removeFirst();
                gameData.addToExile(targetPlayerId, card);
                exiledThisProcess.add(card.getId());
                gameLogService.append(gameData, GameLog.builder().text(controllerName + " exiles ")
                        .card(card).text(" from " + targetName + "'s library (" + sourceName + ").").build());
            }
        }

        List<UUID> castableSpellIds = new ArrayList<>();
        for (UUID cardId : exiledThisProcess) {
            Card card = gameData.findExiledCard(cardId).card();
            if (isCastableSpell(card) && card.getManaValue() <= x) {
                castableSpellIds.add(cardId);
            }
        }

        if (castableSpellIds.isEmpty()) {
            log.info("Game {} - {} found no castable spells (mana value {} or less) among exiled cards",
                    gameData.id, sourceName, x);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, castableSpellIds.size()));
        log.info("Game {} - {} awaiting cast choices for {} exiled spells",
                gameData.id, sourceName, castableSpellIds.size());
    }

    private static boolean isCastableSpell(Card card) {
        if (card.hasType(CardType.LAND)) {
            return false;
        }
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        if (card.getType().isPermanentType() && card.getType() != CardType.LAND) {
            return true;
        }
        return card.getAdditionalTypes().stream()
                .anyMatch(type -> type.isPermanentType() && type != CardType.LAND);
    }
}
