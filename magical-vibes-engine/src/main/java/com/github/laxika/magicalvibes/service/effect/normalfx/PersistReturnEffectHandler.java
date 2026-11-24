package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PersistReturnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Persist and undying (CR 702.79a, 702.93a): returns the dying card from the graveyard stay its death put it in
 * (CR 400.7) to the battlefield under its owner's control with one {@link PersistReturnEffect#counterType()} counter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistReturnEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PersistReturnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PersistReturnEffect dieReturn = (PersistReturnEffect) effect;
        String keyword = dieReturn.keywordName();
        Card card = entry.getCard();
        if (entry.sourceCardGraveyardEntryVersion() >= 0
                && entry.sourceCardGraveyardEntryVersion() != gameData.graveyardEntryVersion(card.getId())) {
            return;
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            log.info("Game {} - {} {} return fizzles (no longer in a graveyard)", gameData.id, card.getName(), keyword);
            return;
        }

        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield, so the
        // return does nothing and the card stays in the graveyard.
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " can't return from the graveyard (" + keyword + "); it stays in the graveyard."));
            log.info("Game {} - {} {} return blocked (can't enter from a graveyard)", gameData.id, card.getName(), keyword);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        // The creature isn't on the battlefield yet, so its owner is the entering controller for the counter
        // replacements (Vizier of Remedies, Hardened Scales); a -1/-1 counter replaced to none re-persists.
        CounterType counterType = dieReturn.counterType();
        int counters = counterType == CounterType.PLUS_ONE_PLUS_ONE
                ? gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, ownerId, 1)
                : gameQueryService.replaceCounters(gameData, permanent, ownerId, counterType, 1);
        permanent.setCounterCount(counterType, counters);
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(ownerId);
        String counterText = counterType == CounterType.PLUS_ONE_PLUS_ONE ? "+1/+1" : "-1/-1";
        String suffix = counters == 0 ? " to the battlefield (" + keyword + ")."
                : counters == 1 ? " to the battlefield with a " + counterText + " counter (" + keyword + ")."
                : " to the battlefield with " + counters + " " + counterText + " counters (" + keyword + ").";
        gameLogService.append(gameData, GameLog.textCardText(playerName + " returns ", card, suffix));
        log.info("Game {} - {} returns via {} with {} {} counter(s)", gameData.id, card.getName(), keyword,
                counters, counterType);

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, ownerId, permanent, card);
    }
}
