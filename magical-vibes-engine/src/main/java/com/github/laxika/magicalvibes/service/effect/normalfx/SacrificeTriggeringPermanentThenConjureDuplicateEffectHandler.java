package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentThenConjureDuplicateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an ally-enter trigger that replaces its entering permanent with a fresh copy. */
@Component
@RequiredArgsConstructor
public class SacrificeTriggeringPermanentThenConjureDuplicateEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTriggeringPermanentThenConjureDuplicateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var replacement = (SacrificeTriggeringPermanentThenConjureDuplicateEffect) effect;
        if (entry.getTriggeringPermanentId() == null) {
            return;
        }

        Permanent triggeringPermanent = gameQueryService.findPermanentById(
                gameData, entry.getTriggeringPermanentId());
        Card sourceCard = triggeringPermanent != null
                ? triggeringPermanent.getCard() : entry.getTriggeringCardSnapshot();
        if (sourceCard == null) {
            return;
        }

        // The duplicate doesn't depend on the sacrifice: if the permanent already left or can't be
        // sacrificed, its last known information is still conjured.
        if (triggeringPermanent != null) {
            var currentControllerId = gameQueryService.findPermanentController(gameData, triggeringPermanent.getId());
            if (entry.getControllerId().equals(currentControllerId)
                    && !gameQueryService.cantBeSacrificed(gameData, triggeringPermanent)
                    && permanentRemovalService.sacrificePermanentToGraveyard(gameData, triggeringPermanent)) {
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                        gameData, currentControllerId, sourceCard);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard, " is sacrificed."));
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        Card duplicateCard = TokenCopySupport.buildTokenCopyCard(sourceCard, replacement.copyProfile());
        duplicateCard.setToken(false);
        duplicateCard.setConjured(true);
        duplicateCard.setOwnerId(entry.getControllerId());
        Permanent duplicate = new Permanent(duplicateCard);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, entry.getControllerId(), duplicate,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        entry.getCreatedPermanentIds().add(duplicate.getId());
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicateCard, " is conjured onto the battlefield."));
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);

        if (duplicateCard.hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, entry.getControllerId(), duplicateCard);
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), duplicateCard, null, false);
        }
    }
}
