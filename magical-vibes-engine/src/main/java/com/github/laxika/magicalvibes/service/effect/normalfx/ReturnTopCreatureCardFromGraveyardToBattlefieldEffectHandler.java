package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardOwner;
import com.github.laxika.magicalvibes.model.effect.ReturnTopCreatureCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Puts the topmost creature card of the selected graveyard onto the battlefield under the effect
 * controller's control. Cards above it that are not creature cards are skipped, so a graveyard whose
 * literal top card is a noncreature still reanimates the creature card below it. No-op when that
 * graveyard holds no creature card.
 * <p>
 * When {@code assignNoCombatDamageIfReturned} is set, the stack entry's source permanent assigns no
 * combat damage this turn — but only when a creature card was actually returned.
 */
@Component
@RequiredArgsConstructor
public class ReturnTopCreatureCardFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTopCreatureCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnTopCreatureCardFromGraveyardToBattlefieldEffect returnEffect =
                (ReturnTopCreatureCardFromGraveyardToBattlefieldEffect) effect;

        UUID controllerId = entry.getControllerId();
        UUID graveyardOwnerId = returnEffect.graveyard() == GraveyardOwner.TARGET_PLAYER
                ? entry.getTargetId()
                : controllerId;
        if (graveyardOwnerId == null) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null) {
            return;
        }

        for (int index = graveyard.size() - 1; index >= 0; index--) {
            Card card = graveyard.get(index);
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }
            graveyard.remove(index);
            graveyardService.notifyCardsLeftGraveyard(gameData, graveyardOwnerId);
            graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, card);
            if (returnEffect.assignNoCombatDamageIfReturned()) {
                assignNoCombatDamage(gameData, entry);
            }
            return;
        }
    }

    private void assignNoCombatDamage(GameData gameData, StackEntry entry) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        gameData.creaturesPreventedFromDealingCombatDamage.add(sourcePermanentId);
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        String sourceName = source != null ? source.getCard().getName() : "the attacking creature";
        gameLogService.append(gameData,
                GameLog.text(sourceName + " assigns no combat damage this turn."));
    }
}
