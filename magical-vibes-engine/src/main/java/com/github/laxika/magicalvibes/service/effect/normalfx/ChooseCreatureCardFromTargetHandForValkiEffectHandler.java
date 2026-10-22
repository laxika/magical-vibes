package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingValkiHandExileChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardFromTargetHandForValkiEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseCreatureCardFromTargetHandForValkiEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureCardFromTargetHandForValkiEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = ((ChooseCreatureCardFromTargetHandForValkiEffect) effect).targetPlayerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null
                || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            // Valki already left: the hand is still revealed, but the card does not move (CR 610.3b).
            cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        boolean hasCreature = hand != null && hand.stream().anyMatch(card -> card.hasType(CardType.CREATURE));
        if (hasCreature) {
            gameData.queueInteraction(new PendingValkiHandExileChoice(sourcePermanentId));
        }

        UUID previousTargetId = entry.getTargetId();
        entry.setTargetIdForEffectResolution(targetPlayerId);
        try {
            playerInteractionSupport.resolveHandRevealAndChoose(
                    gameData, entry, 1, List.of(), List.of(CardType.CREATURE), null,
                    false, true, sourcePermanentId, false, false);
        } finally {
            entry.restoreTargetIdAfterEffectResolution(previousTargetId);
        }
    }
}
