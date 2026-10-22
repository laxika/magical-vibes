package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KinshipEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KinshipEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent livePermanent = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        Permanent source = livePermanent != null ? livePermanent : entry.getSourcePermanentSnapshot();
        if (effect instanceof KinshipEffect kinship && kinship.mayLook()
                && !gameData.playerDecks.getOrDefault(controllerId, List.of()).isEmpty()) {
            KinshipEffect lookStep = new KinshipEffect(kinship.revealEffects(), true, true);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId, List.of(lookStep),
                    entry.getCard().getName() + " — Look at the top card of your library?",
                    null, null, entry.getSourcePermanentId(), null, 0, 0, null, null, null,
                    source == null ? null : new Permanent(source), controllerId, null, 0, null, null, null,
                    null));
            return;
        }
        lookAtTopCard(gameData, controllerId, entry.getCard(), entry.getSourcePermanentId(), source, effect);
    }

    /**
     * Looks at the top card of {@code controllerId}'s library and, if it shares a creature type with
     * {@code source} (its last known information when it has left the battlefield), offers the
     * "you may reveal" choice.
     */
    public void lookAtTopCard(GameData gameData, UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                              Permanent source, CardEffect effect) {
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = sourceCard.getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));
        cardRevealService.revealToPlayer(gameData, controllerId, GameEventFact.RevealZone.LIBRARY,
                List.of(topCard), controllerId);
        log.info("Game {} - {} looks at top card via Kinship: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        if (source == null) {
            return;
        }

        if (!gameQueryService.shareCreatureType(gameData, source, topCard, controllerId)) {
            gameLogService.append(gameData, GameLog.text("The top card does not share a creature type with " + sourceName + "."));
            log.info("Game {} - top card {} shares no creature type with {}", gameData.id, topCard.getName(), sourceName);
            return;
        }

        // Shares a creature type — offer the "you may reveal" choice (KinshipMayAbilityHandler completes it).
        KinshipEffect revealStep = effect instanceof KinshipEffect k
                ? new KinshipEffect(k.revealEffects(), k.mayLook(), false) : null;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                controllerId,
                List.of(revealStep != null ? revealStep : effect),
                sourceName + " — Reveal " + topCard.getName() + "?",
                null,
                null,
                sourcePermanentId
        ));
    }
}
