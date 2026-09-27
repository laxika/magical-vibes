package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderControlEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnExiledCardToBattlefieldUnderControlEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnExiledCardToBattlefieldUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnExiledCardToBattlefieldUnderControlEffect returnEffect =
                (ReturnExiledCardToBattlefieldUnderControlEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(returnEffect.exiledCardId());
        if (exiled == null || !gameData.removeFromExile(returnEffect.exiledCardId())) {
            return;
        }

        Card card = exiled.card();
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);

        if (!controllerId.equals(exiled.ownerId())) {
            gameData.stolenCreatures.put(permanent.getId(), exiled.ownerId());
            creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    EffectDuration.PERMANENT, null, entry.getCard().getName());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " returns to the battlefield under " + playerName + "'s control."));
        log.info("Game {} - {} returns under {}'s control", gameData.id, card.getName(), playerName);
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
    }
}
