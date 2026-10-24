package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
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
public class SacrificeUnlessReturnOwnPermanentTypeToHandEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final StepDepartureSupport stepDepartureSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeUnlessReturnOwnPermanentTypeToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeUnlessReturnOwnPermanentTypeToHandEffect) effect;

        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Find the source permanent on the battlefield
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        // Check if the controller has any permanents of the required type on the battlefield
        boolean hasValidPermanent = false;
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.getEffectiveCardTypes(gameData, p).contains(e.permanentType())
                        && !(e.excludeSource() && p.getCard().getId().equals(sourceCard.getId()))) {
                    hasValidPermanent = true;
                    break;
                }
            }
        }

        String typeName = e.permanentType().name().toLowerCase();

        if (!hasValidPermanent) {
            if (sourcePermanent != null) {
                gameLogService.append(gameData, GameLog.text(playerName + " controls no " + typeName + "s."));
                stepDepartureSupport.sacrifice(gameData, entry, effect, sourcePermanent.getId());
                log.info("Game {} - {} sacrificed (no {}s to return)", gameData.id, sourceCard.getName(), typeName);
            }
            return;
        }

        // Has valid permanents — ask the controller via the may ability system
        String prompt;
        if (sourcePermanent != null) {
            prompt = "Return an " + typeName + " you control to hand? If you don't, " + sourceCard.getName() + " will be sacrificed.";
        } else {
            prompt = sourceCard.getName() + " is no longer on the battlefield. Return an " + typeName + " you control to hand anyway?";
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, controllerId, List.of(e), prompt
        ));
    
    }
}
