package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprovisationCapstoneEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
public class ImprovisationCapstoneEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ImprovisationCapstoneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ImprovisationCapstoneEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        List<UUID> exiledThisProcess = new ArrayList<>();
        int totalManaValue = 0;

        while (totalManaValue < e.totalManaValueThreshold() && !deck.isEmpty()) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            exiledThisProcess.add(card.getId());
            totalManaValue += card.getManaValue();
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " exiles " + card.getName() + " (mana value " + card.getManaValue() + ") (" + sourceName + ").");
        }

        if (deck.isEmpty() && totalManaValue < e.totalManaValueThreshold()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + "'s library ran out before reaching total mana value "
                            + e.totalManaValueThreshold() + " (" + sourceName + ").");
        }

        List<UUID> castableSpellIds = new ArrayList<>();
        for (UUID cardId : exiledThisProcess) {
            Card exiled = gameData.findExiledCard(cardId).card();
            if (isCastableSpell(exiled)) {
                castableSpellIds.add(cardId);
            }
        }

        if (castableSpellIds.isEmpty()) {
            log.info("Game {} - {} found no castable spells among exiled cards", gameData.id, sourceName);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, castableSpellIds.size()));
        log.info("Game {} - {} awaiting cast choices for {} exiled spells",
                gameData.id, sourceName, castableSpellIds.size());
    }

    private static boolean isCastableSpell(Card card) {
        return card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)
                || card.hasType(CardType.CREATURE) || card.hasType(CardType.ARTIFACT)
                || card.hasType(CardType.ENCHANTMENT) || card.hasType(CardType.PLANESWALKER);
    }
}
