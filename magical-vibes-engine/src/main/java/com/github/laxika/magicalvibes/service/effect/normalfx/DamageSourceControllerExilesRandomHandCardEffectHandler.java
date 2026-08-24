package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerExilesRandomHandCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardOntoBattlefieldUnderControllerEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Rona's back-face damage trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageSourceControllerExilesRandomHandCardEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageSourceControllerExilesRandomHandCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceControllerId = ((DamageSourceControllerExilesRandomHandCardEffect) effect).sourceControllerId();
        if (sourceControllerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(sourceControllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card exiled = hand.get(randomIndex);
        String playerName = gameData.playerIdToName.get(sourceControllerId);

        cardRevealService.revealToAllPlayers(
                gameData, sourceControllerId, GameEventFact.RevealZone.HAND, List.of(exiled));
        hand.remove(randomIndex);
        exileService.exileCard(gameData, sourceControllerId, exiled);

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ").card(exiled)
                .text(" at random from their hand.").build());

        if (exiled.hasType(CardType.LAND)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(new PutExiledCardOntoBattlefieldUnderControllerEffect(exiled.getId())),
                    "Put " + exiled.getName() + " onto the battlefield under your control?"));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + exiled.getName() + " without paying its mana cost?",
                exiled.getId()));
        log.info("Game {} - {} offers {} to {} for free",
                gameData.id, entry.getCard().getName(), exiled.getName(),
                gameData.playerIdToName.get(entry.getControllerId()));
    }
}
