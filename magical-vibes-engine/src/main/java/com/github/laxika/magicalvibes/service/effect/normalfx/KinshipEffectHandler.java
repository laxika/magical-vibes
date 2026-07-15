package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
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
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KinshipEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));
        log.info("Game {} - {} looks at top card via Kinship: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        if (source == null) {
            return;
        }

        if (!sharesCreatureType(source, topCard)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text("The top card does not share a creature type with " + sourceName + "."));
            log.info("Game {} - top card {} shares no creature type with {}", gameData.id, topCard.getName(), sourceName);
            return;
        }

        // Shares a creature type — offer the "you may reveal" choice (KinshipMayAbilityHandler completes it).
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(effect),
                sourceName + " — Reveal " + topCard.getName() + "?",
                null,
                null,
                entry.getSourcePermanentId()
        ));
    }

    private boolean sharesCreatureType(Permanent source, Card topCard) {
        List<CardSubtype> sourceTypes = new ArrayList<>(source.getCard().getSubtypes());
        sourceTypes.addAll(source.getTransientSubtypes());
        boolean sourceChangeling = source.hasKeyword(Keyword.CHANGELING);

        List<CardSubtype> topTypes = topCard.getSubtypes();
        boolean topChangeling = topCard.getKeywords().contains(Keyword.CHANGELING);

        return (sourceChangeling && (topChangeling || !topTypes.isEmpty()))
                || (topChangeling && !sourceTypes.isEmpty())
                || sourceTypes.stream().anyMatch(topTypes::contains);
    }
}
