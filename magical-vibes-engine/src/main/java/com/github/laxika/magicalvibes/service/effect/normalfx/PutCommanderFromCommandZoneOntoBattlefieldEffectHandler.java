package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCommanderFromCommandZoneOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCommanderFromCommandZoneOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCommanderFromCommandZoneOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> commandZone = gameData.playerCommandZones.getOrDefault(controllerId, List.of());
        if (commandZone.isEmpty()) {
            return;
        }
        if (commandZone.size() > 1) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.CommanderBattlefieldChoice(
                            controllerId, new ArrayList<>(commandZone), entry.getCard()));
            return;
        }
        putOntoBattlefield(gameData, controllerId, commandZone.getFirst(), entry.getCard());
    }

    public void completeChoice(GameData gameData, UUID playerId, UUID cardId, Card sourceCard) {
        List<Card> commandZone = gameData.playerCommandZones.getOrDefault(playerId, List.of());
        Card commander = commandZone.stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
        if (commander != null) {
            putOntoBattlefield(gameData, playerId, commander, sourceCard);
        }
    }

    private void putOntoBattlefield(GameData gameData, UUID controllerId, Card commander, Card sourceCard) {
        List<Card> commandZone = gameData.playerCommandZones.get(controllerId);
        if (commandZone == null || !commandZone.remove(commander)) {
            return;
        }

        Permanent permanent = new Permanent(commander, Zone.COMMAND);
        permanent.setCommander(true);
        gameData.addEffectOnPermanent(sourceCard.getName(), null, controllerId,
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET), permanent.getId(), Duration.PERMANENT);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, commander);
        DelayedPermanentActions.schedule(gameData, controllerId, sourceCard, null,
                new DelayedPermanentAction(permanent.getId(),
                        DelayedPermanentActionKind.RETURN_TO_COMMAND_ZONE_AT_END_STEP),
                ActivePlayer.ANY);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(
                commander, gameData.playerIdToName.get(controllerId)));
    }
}
