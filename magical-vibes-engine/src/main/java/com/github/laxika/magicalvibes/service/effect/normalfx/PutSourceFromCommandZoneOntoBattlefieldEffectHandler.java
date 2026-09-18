package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceFromCommandZoneOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutSourceFromCommandZoneOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSourceFromCommandZoneOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> commandZone = gameData.playerCommandZones.get(controllerId);
        if (commandZone == null) {
            return;
        }

        Card sourceCard = commandZone.stream()
                .filter(card -> card.getId().equals(entry.getCard().getId()))
                .findFirst()
                .orElse(null);
        if (sourceCard == null) {
            return;
        }

        commandZone.remove(sourceCard);
        Permanent permanent = new Permanent(sourceCard, Zone.COMMAND);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, sourceCard);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(
                sourceCard, gameData.playerIdToName.get(controllerId)));
    }
}
