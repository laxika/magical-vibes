package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
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
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an ally-enter trigger that replaces its entering permanent with a fresh copy. */
@Component
@RequiredArgsConstructor
public class SacrificeTriggeringPermanentThenConjureDuplicateEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;
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
        if (triggeringPermanent != null
                && entry.getControllerId().equals(
                        gameQueryService.findPermanentController(gameData, triggeringPermanent.getId()))
                && !gameQueryService.cantBeSacrificed(gameData, triggeringPermanent)) {
            if (entry.getTriggeringCardSnapshot() == null) {
                entry.setTriggeringCardSnapshot(sourceCard);
            }
            stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, triggeringPermanent.getId(), this);
            return;
        }
        conjureDuplicate(gameData, entry, replacement, sourceCard);
    }

    /** "Conjure a duplicate of it": its last-known information, after it was sacrificed. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent triggeringPermanent = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        Card sourceCard = triggeringPermanent != null
                ? triggeringPermanent.getCard() : entry.getTriggeringCardSnapshot();
        if (sourceCard != null) {
            conjureDuplicate(gameData, entry, (SacrificeTriggeringPermanentThenConjureDuplicateEffect) effect,
                    sourceCard);
        }
    }

    private void conjureDuplicate(GameData gameData, StackEntry entry,
                                  SacrificeTriggeringPermanentThenConjureDuplicateEffect replacement, Card sourceCard) {
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
