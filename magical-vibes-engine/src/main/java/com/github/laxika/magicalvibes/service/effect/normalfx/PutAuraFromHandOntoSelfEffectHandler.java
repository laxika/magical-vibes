package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutAuraFromHandOntoSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAuraFromHandOntoSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                UUID controllerId = entry.getControllerId();

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
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (no longer on the battlefield)."));
                    log.info("Game {} - {} ETB fizzles, creature left battlefield", gameData.id, entry.getCard().getName());
                    return;
                }

                List<Card> hand = gameData.playerHands.get(controllerId);
                List<Integer> auraIndices = new ArrayList<>();
                if (hand != null) {
                    for (int i = 0; i < hand.size(); i++) {
                        if (hand.get(i).isAura()) {
                            auraIndices.add(i);
                        }
                    }
                }

                if (auraIndices.isEmpty()) {
                    String playerName = gameData.playerIdToName.get(controllerId);
                    String logEntry = playerName + " has no Aura cards in hand.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} has no Auras in hand for {} ETB", gameData.id, playerName, entry.getCard().getName());
                    return;
                }

                String prompt = "You may put an Aura card from your hand onto the battlefield attached to " + entry.getCard().getName() + ".";
                playerInputService.beginTargetedCardChoice(gameData, controllerId, auraIndices, prompt, self.getId());
    
    }
}
