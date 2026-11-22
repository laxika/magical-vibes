package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndReattachAuraOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
        Long diedInStay = entry.getEventCardGraveyardEntryVersions().get(dyingCreatureCardId);
        // A card that left the graveyard and came back is a new object (CR 400.7); an instant or sorcery card that was
        // a face-down creature remains there (CR 400.4a).
        if (creatureCard != null && (diedInStay != null
                && diedInStay != gameData.graveyardEntryVersion(dyingCreatureCardId)
                || gameQueryService.cantEnterFaceUp(creatureCard))) {
            creatureCard = null;
        }
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
        long auraStay = entry.sourceCardGraveyardEntryVersion();
        if (gameQueryService.findCardInGraveyardById(gameData, auraCardId) != null
                && (auraStay < 0 || auraStay == gameData.graveyardEntryVersion(auraCardId))) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                            new PutCardsOntoBattlefieldEffect(Subjects.sourceCard())
                                    .underControlOf(Subjects.you())
                                    .attachedTo(Subjects.named(returnedCreature.getId()))),
                    entry.getSourcePermanentId(), null, null,
                    DelayedReferents.graveyardCard(auraCardId, gameData.graveyardEntryVersion(auraCardId))));
        }
    }
}
