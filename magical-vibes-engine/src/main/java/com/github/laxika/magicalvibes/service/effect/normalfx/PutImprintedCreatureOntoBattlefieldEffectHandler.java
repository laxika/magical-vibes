package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutImprintedCreatureOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutImprintedCreatureOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        Card imprintedCard = entry.getCard().getImprintedCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (imprintedCard == null) {
            String logMsg = entry.getCard().getName() + "'s imprint ability resolves but no card was imprinted.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String revealLog = playerName + " turns the exiled card face up: " + imprintedCard.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        boolean isCreature = imprintedCard.hasType(CardType.CREATURE);

        if (!isCreature) {
            String notCreatureLog = imprintedCard.getName() + " is not a creature card. It remains in exile.";
            gameBroadcastService.logAndBroadcast(gameData, notCreatureLog);
            return;
        }

        // Remove from exile zone
        gameData.removeFromExile(imprintedCard.getId());

        // Put onto the battlefield
        Permanent perm = new Permanent(imprintedCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String enterLog = imprintedCard.getName() + " enters the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, enterLog);

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, perm, imprintedCard);

        log.info("Game {} - {} puts imprinted creature {} onto battlefield",
                gameData.id, playerName, imprintedCard.getName());
    }
}
