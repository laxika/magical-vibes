package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect) effect;
        Permanent selected = gameQueryService.findPermanentById(gameData, conjure.permanentId());
        if (selected == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Card conjuredCard = selected.getCard().createConjuredCopy();
        conjuredCard.setOwnerId(controllerId);
        if (conjure.power() != null) {
            conjuredCard.setPower(conjure.power());
        }
        if (conjure.toughness() != null) {
            conjuredCard.setToughness(conjure.toughness());
        }
        Permanent permanent = new Permanent(conjuredCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of());
        triggerCollectionService.checkConjureTriggers(gameData, controllerId, 1);
        if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) {
            return;
        }

        entry.getCreatedPermanentIds().add(permanent.getId());
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a duplicate of " + selected.getCard().getName() + " onto the battlefield."));
        if (conjuredCard.hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, conjuredCard);
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, conjuredCard, null, false);
        }
    }
}
