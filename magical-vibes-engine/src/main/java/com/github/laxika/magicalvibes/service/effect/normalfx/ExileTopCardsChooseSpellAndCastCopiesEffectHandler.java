package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseSpellAndCastCopiesEffect;
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
public class ExileTopCardsChooseSpellAndCastCopiesEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsChooseSpellAndCastCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsChooseSpellAndCastCopiesEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null) {
            return;
        }
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        List<UUID> instantOrSorceryIds = new ArrayList<>();
        for (int i = 0; i < e.count() && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles ").card(card).text(" (" + sourceName + ").").build());
            if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
                instantOrSorceryIds.add(card.getId());
            }
        }

        if (instantOrSorceryIds.isEmpty()) {
            log.info("Game {} - {} exiled no instant or sorcery card", gameData.id, sourceName);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ExiledSpellCopyChoice(controllerId, instantOrSorceryIds, e.copies()));
        log.info("Game {} - {} awaiting copy choice among {} exiled spells",
                gameData.id, sourceName, instantOrSorceryIds.size());
    }
}
