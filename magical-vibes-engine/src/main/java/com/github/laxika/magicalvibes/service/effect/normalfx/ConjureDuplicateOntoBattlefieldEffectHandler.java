package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ConjureDuplicateOntoBattlefieldEffect}: each duplicate is a new conjured card owned and controlled by
 * the effect's controller, all of them entering as one event, with their enter handling once every one is on the
 * battlefield. Each is recorded as created this resolution, for "it gains haste" and the delayed "shuffle it into its
 * owner's library", and each is one conjure.
 */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureDuplicateOntoBattlefieldEffect) effect;
        List<Card> originals = subjectResolutionService.cardsToDuplicate(gameData, entry, effect, conjure.of());
        if (originals.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> entered = new ArrayList<>();
        boolean alreadyCollectingEntries = gameData.collectingSimultaneousCreatureEntryTriggers;
        gameData.collectingSimultaneousCreatureEntryTriggers = true;
        if (!alreadyCollectingEntries) {
            gameData.simultaneousCreatureEntryTriggers.clear();
        }
        try {
            for (Card original : originals) {
                Card duplicate = original.createConjuredCopy();
                duplicate.setOwnerId(controllerId);
                if (conjure.basePower() != null) {
                    duplicate.setPower(conjure.basePower());
                    duplicate.setToughness(conjure.baseToughness());
                }
                Permanent permanent = new Permanent(duplicate);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent,
                        enterTappedTypes, List.copyOf(entered));
                ActionEvents.conjured(gameData, controllerId, 1);
                if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) {
                    continue;
                }

                entered.add(permanent);
                entry.getCreatedPermanentIds().add(permanent.getId());
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        " conjures a duplicate of " + Permanent.displayName(duplicate) + " onto the battlefield."));
            }
            for (Permanent permanent : entered) {
                Card duplicate = permanent.getCard();
                if (duplicate.hasType(CardType.LAND)) {
                    battlefieldEntryService.processLandETBEffects(gameData, controllerId, duplicate);
                } else {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(
                            gameData, controllerId, duplicate, null, false);
                }
            }
        } finally {
            gameData.collectingSimultaneousCreatureEntryTriggers = alreadyCollectingEntries;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }
    }
}
