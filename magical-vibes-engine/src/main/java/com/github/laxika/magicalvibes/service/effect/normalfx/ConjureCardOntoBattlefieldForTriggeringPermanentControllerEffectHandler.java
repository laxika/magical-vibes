package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves a battlefield conjure for the controller of the permanent that caused the trigger, as it is or last was
 * (CR 608.2h): that player conjures the printing's card, so their "whenever you conjure" abilities trigger.
 */
@Component
@RequiredArgsConstructor
public class ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect conjure =
                (ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect) effect;
        UUID controllerId = subjectResolutionService.player(
                gameData, entry, effect, Subjects.controllerOf(Subjects.triggering()));
        if (controllerId == null || !gameData.playerIds.contains(controllerId)) {
            return;
        }

        CardSet set = CardSet.findByCode(conjure.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + conjure.setCode());
        }
        Card card = cardCatalog.findByCollectorNumber(set, conjure.collectorNumber()).createCard();
        card.setConjured(true);
        card.setOwnerId(controllerId);
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, controllerId, permanent, conjure.enterTappedTypes());
        ActionEvents.conjured(gameData, controllerId, 1);
        if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) {
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " conjures ", card, " onto the battlefield."));
        if (card.hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, card);
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, card, null, false);
        }
    }
}
