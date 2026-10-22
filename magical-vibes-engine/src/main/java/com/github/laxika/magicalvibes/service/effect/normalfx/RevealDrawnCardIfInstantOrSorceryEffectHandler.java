package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardIfInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Logs the reveal of a drawn card and queues the "whenever you reveal ... this way" trigger. */
@Component
@RequiredArgsConstructor
public class RevealDrawnCardIfInstantOrSorceryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealDrawnCardIfInstantOrSorceryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        reveal(gameData, entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId(),
                (RevealDrawnCardIfInstantOrSorceryEffect) effect);
    }

    /** Reveals the drawn card; an instant or sorcery puts the token-creating trigger on the stack. */
    public void reveal(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                       RevealDrawnCardIfInstantOrSorceryEffect reveal) {
        Card drawn = reveal.drawnCard();
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " reveals ")
                .card(drawn)
                .text(" with ")
                .card(sourceCard)
                .text(".")
                .build());
        if (!drawn.hasType(CardType.INSTANT) && !drawn.hasType(CardType.SORCERY)) {
            return;
        }
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(reveal.triggeredEffect())),
                null,
                sourcePermanentId));
    }
}
