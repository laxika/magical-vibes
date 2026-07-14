package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfWithinXManaValueOrExileEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles {@link PutAuraFromHandOntoSelfWithinXManaValueOrExileEffect}: the controller may put an Aura
 * card from hand with mana value <= X (the ability's paid X) onto the battlefield attached to the source
 * creature. If no eligible Aura is available, or the controller declines, the source creature is exiled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutAuraFromHandOntoSelfWithinXManaValueOrExileEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAuraFromHandOntoSelfWithinXManaValueOrExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int maxManaValue = entry.getXValue();

        Permanent self = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(entry.getCard().getId())) {
                    self = p;
                    break;
                }
            }
        }

        if (self == null) {
            // The creature already left the battlefield — nothing to attach to or exile.
            log.info("Game {} - {} left the battlefield before its Aura-or-exile step", gameData.id, entry.getCard().getName());
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Integer> auraIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card c = hand.get(i);
                if (c.isAura() && c.getManaValue() <= maxManaValue) {
                    auraIndices.add(i);
                }
            }
        }

        if (auraIndices.isEmpty()) {
            // No eligible Aura in hand — "If you don't, exile this creature."
            permanentRemovalService.removePermanentToExile(gameData, self);
            gameBroadcastService.logAndBroadcast(gameData, self.getCard().getName() + " is exiled.");
            log.info("Game {} - {} exiled (no Aura with mana value {} or less in hand)",
                    gameData.id, entry.getCard().getName(), maxManaValue);
            return;
        }

        String prompt = "You may put an Aura card with mana value " + maxManaValue + " or less from your hand onto "
                + "the battlefield attached to " + entry.getCard().getName() + ". If you don't, it is exiled.";
        playerInputService.beginTargetedCardChoice(gameData, controllerId, auraIndices, prompt, self.getId(), self.getId());
    }
}
