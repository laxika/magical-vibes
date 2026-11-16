package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndReattachAuraOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardAttachedToPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnEnchantedCreatureAndReattachAuraOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureAndReattachAuraOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "That card": the card the enchanted permanent put into the graveyard.
        UUID dyingCreatureCardId = entry.getTriggeringCards().isEmpty()
                ? null : entry.getTriggeringCards().getFirst().getId();
        if (dyingCreatureCardId == null) {
            log.info("Game {} - {} death trigger fizzles (no dying creature card)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card creatureCard = gameQueryService.findCardInGraveyardById(gameData, dyingCreatureCardId);
        if (creatureCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (creature not in graveyard)."));
            return;
        }

        UUID creatureOwnerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCreatureCardId);
        if (creatureOwnerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCreatureCardId);
        Permanent returnedCreature = graveyardReturnSupport.putCardOntoBattlefield(
                gameData, creatureOwnerId, creatureCard, null, null, false, false, null);
        if (returnedCreature == null) {
            return;
        }

        UUID auraCardId = entry.getCard().getId();
        if (gameQueryService.findCardInGraveyardById(gameData, auraCardId) != null) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                            new ReturnAuraFromGraveyardAttachedToPermanentEffect(
                                    auraCardId, entry.getControllerId(), returnedCreature.getId())),
                    entry.getSourcePermanentId(), null, null,
                    DelayedReferents.graveyardCard(auraCardId, gameData.graveyardEntryVersion(auraCardId))));
        }
    }
}
