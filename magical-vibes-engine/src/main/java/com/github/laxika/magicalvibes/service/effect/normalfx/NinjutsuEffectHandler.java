package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NinjutsuEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link NinjutsuEffect}: puts the source card from its controller's hand onto the
 * battlefield tapped and attacking (CR 702.49a).
 *
 * <p>The ninja enters attacking the same player or planeswalker the creature returned as part of the
 * cost was attacking (CR 702.49c) — that defender was captured at activation time and travels on the
 * effect. It was never declared as an attacker, so no attack triggers or attack legality checks apply
 * (CR 508.4).
 *
 * <p>If the card has left the owner's hand in response (discarded, milled, stolen), the ability does
 * nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NinjutsuEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NinjutsuEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        NinjutsuEffect e = (NinjutsuEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card card = entry.getCard();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (card == null || hand == null || !hand.remove(card)) {
            return;
        }

        Permanent permanent = new Permanent(card);
        permanent.tap();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        permanent.setAttacking(true);
        permanent.setAttackTarget(e.attackTargetId());

        gameLogService.append(gameData, GameLog.cardThen(card, " enters the battlefield tapped and attacking."));
        log.info("Game {} - {} enters tapped and attacking via ninjutsu", gameData.id, card.getName());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
    }
}
