package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves fixed-count top-library effects that offer any number of exiled spells for free. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsAndMayCastSpellsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsAndMayCastSpellsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsAndMayCastSpellsEffect) effect;
        if (e.count() <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        List<UUID> castableSpellIds = new ArrayList<>();

        for (int i = 0; i < e.count() && deck != null && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles ")
                    .card(card)
                    .text(" from the top of their library.")
                    .build());

            if (isSpell(card)) {
                castableSpellIds.add(card.getId());
            }
        }

        if (castableSpellIds.isEmpty()) {
            log.info("Game {} - {} found no spells among the exiled cards", gameData.id, entry.getCard().getName());
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, castableSpellIds.size()));
        log.info("Game {} - {} awaiting cast choices for {} exiled spells",
                gameData.id, entry.getCard().getName(), castableSpellIds.size());
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
