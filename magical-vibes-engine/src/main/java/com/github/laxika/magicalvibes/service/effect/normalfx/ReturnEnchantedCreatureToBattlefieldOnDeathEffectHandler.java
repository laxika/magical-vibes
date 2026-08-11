package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ReturnEnchantedCreatureToBattlefieldOnDeathEffect}: returns the creature card
 * that died while enchanted by the source Aura from its owner's graveyard to the battlefield.
 *
 * <p>With {@code underAuraControllersControl} the Aura's controller gets the permanent (False
 * Demise, Unhallowed Pact); when that differs from the card's owner the permanent is tracked as a
 * stolen creature so the control change sticks. Otherwise the owner gets it back (Abduction).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnEnchantedCreatureToBattlefieldOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureToBattlefieldOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnEnchantedCreatureToBattlefieldOnDeathEffect) effect;

        UUID dyingCreatureCardId = e.dyingCreatureCardId();
        if (dyingCreatureCardId == null) {
            log.info("Game {} - {} death trigger fizzles (no dying creature card ID)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card creatureCard = gameQueryService.findCardInGraveyardById(gameData, dyingCreatureCardId);
        if (creatureCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (creature not in graveyard)."));
            log.info("Game {} - {} death trigger fizzles (creature card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCreatureCardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCreatureCardId);
        UUID controllerId = e.underAuraControllersControl() ? entry.getControllerId() : ownerId;
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCreatureCardId);

        if (controllerId == null || controllerId.equals(ownerId)) {
            graveyardReturnSupport.putCardOntoBattlefield(gameData, ownerId, creatureCard,
                    null, null, false, false, e.enterWithCounter());
            return;
        }

        Permanent permanent = graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, creatureCard,
                null, null, false, false, e.enterWithCounter());
        if (permanent == null) {
            return;
        }

        // The Aura's controller keeps the creature (CR 613.1b — layer 2, control-changing effects),
        // but the card is still owned by the player whose graveyard it came from. Record that owner
        // so it goes back to their graveyard when it dies (CR 400.3) and so "enters from your
        // graveyard" triggers fire for them rather than for the Aura's controller.
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, ownerId);
    }
}
