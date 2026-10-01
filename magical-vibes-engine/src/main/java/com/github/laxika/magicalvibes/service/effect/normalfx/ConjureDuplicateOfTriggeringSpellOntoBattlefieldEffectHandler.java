package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/** Resolves Dragonsoul Prodigy's duplicate of the triggering Omen card. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card triggeringSpell = entry.getTriggeringCardId() == null
                ? null
                : gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringSpell == null || triggeringSpell.isToken()) return;

        Card duplicate = triggeringSpell.createConjuredCopy();
        duplicate.setToken(false);
        duplicate.setOwnerId(entry.getControllerId());
        EnumSet<Keyword> keywords = duplicate.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(duplicate.getKeywords());
        keywords.add(Keyword.HASTE);
        duplicate.setKeywords(keywords);

        Permanent permanent = new Permanent(duplicate);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) return;

        entry.getCreatedPermanentIds().add(permanent.getId());
        gameData.queueDelayedAction(new DelayedEndStepTrigger(
                entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), permanent.getId(),
                new ShuffleTargetPermanentIntoLibraryEffect()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a duplicate of " + triggeringSpell.getName() + " onto the battlefield."));

        if (duplicate.hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, entry.getControllerId(), duplicate);
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), duplicate, null, false);
        }
    }
}
